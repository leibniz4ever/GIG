package org.eclipse.ptp.gig.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.log.GkleeLog;
import org.eclipse.ptp.gig.log.LogException;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.preferences.GIGPreferencePage;
import org.eclipse.ptp.gig.views.GIGView;
import org.eclipse.ptp.gig.views.ServerTreeItem;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

/*
 * Contains most of the logical code of the plug-in
 */
public class GIGUtilities {

	public enum JobState {
		None,
		Running,
		Canceled
	}

	private static Job job;
	private static Lock jobsLock = new ReentrantLock();
	private static volatile JobState jobState = JobState.None;
	private static Socket socket;
	private static OutputStream os;
	private static InputStream is;

	public static JobState getJobState() {
		return jobState;
	}

	public static void setJobState(JobState newState) {
		jobsLock.lock();
		jobState = newState;
		jobsLock.unlock();
	}

	/*
	 * This will process the source, binary or log file passed to it
	 */
	public static void processSource(IPath filePath) throws IOException, CoreException, InterruptedException {
		// enforce that the file is of the right type
		// also see if binary or log file instead
		String fileExtension = filePath.getFileExtension();
		if (fileExtension.equals("gig")) { //$NON-NLS-1$
			processBinary(filePath);
			return;
		}
		else if (fileExtension.equals("log")) { //$NON-NLS-1$
			processLog(filePath);
			return;
		}
		else if (!fileExtension.equals("cu") && !fileExtension.equals("C") && !fileExtension.equals("c")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// TODO indicate an error
			return;
		}

		// If we are doing a remote execution, now is the time to go over to it
		IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		boolean local = preferenceStore.getBoolean(GIGPreferencePage.LOCAL);
		if (!local) {
			try {
				sendSelection(new IPath[] { filePath });
				requestVerification(filePath);
			} catch (IncorrectPasswordException ipe) {
				// TODO open up dialog that says "Incorrect Password, change it under preferences" or something
			}
			return;
		}

		// make the gig directory which will hold all relevant files
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getFile(filePath).getProject();
		IFolder gigFolder = project.getFolder("gig"); //$NON-NLS-1$
		if (!gigFolder.exists()) {
			// TODO fix null
			gigFolder.create(true, true, null);
		}

		// now prepare the executable path
		IPath folderPath = gigFolder.getFullPath();
		int segments = filePath.segmentCount();
		IPath filename = filePath.removeFirstSegments(segments - 1).removeFileExtension();
		filename = folderPath.append(filename);
		IPath exec = filename.addFileExtension("gig"); //$NON-NLS-1$
		filename = filename.addFileExtension("C"); //$NON-NLS-1$

		/*
		 * copy the file over to the gig folder so that gklee can work entirely in that directory, also gklee requires *.c format
		 * files
		 */
		IFile origFile = (IFile) workspaceRoot.findMember(filePath);
		IResource resource = workspaceRoot.findMember(filename);
		if (resource != null && resource.exists()) {
			// TODO fix null
			resource.delete(true, null);
		}
		// TODO fix null's
		origFile.copy(filename, true, null);
		workspaceRoot.findMember(folderPath).refreshLocal(1, null);

		// begin building the command line with absolute (not relative paths), expecially from the preferenceStore
		String sourceOSPath, binaryOSPath;
		IPath sourceAbsoluteIPath = workspaceRoot.findMember(filename).getLocation();
		sourceOSPath = sourceAbsoluteIPath.toOSString();
		binaryOSPath = sourceAbsoluteIPath.removeFileExtension().addFileExtension("gig").toOSString(); //$NON-NLS-1$
		String kleeOSPath = preferenceStore.getString(GIGPreferencePage.BIN) + "/klee-l++"; //$NON-NLS-1$
		ProcessBuilder processBuilder = new ProcessBuilder(kleeOSPath, sourceOSPath, "-o", binaryOSPath); //$NON-NLS-1$

		// klee depends on a lot of path variables, so set these in the environment
		Map<String, String> environment = processBuilder.environment();
		buildEnvPath(environment);

		// TODO have the output go to console (GIG Console?)
		Process process = processBuilder.start();
		process.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		Scanner scan = new Scanner(br);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			System.out.println(line);
		}
		if (process.exitValue() != 0) {
			processExitOnValue(process.exitValue());
			return;
		}

		// refresh environment so that eclipse is aware of the new binary
		// TODO fix null
		gigFolder.refreshLocal(1, null);

		processBinary(exec);
	}

	private static void sendSelection(IPath[] iPaths) throws IOException, CoreException, IncorrectPasswordException {
		try {
			// TODO redo this to conform with new protocols
			initializeConnection(0);
			sendFolders(new IPath[0]);
			sendFiles(new IPath[] { iPaths[0] });
			closeConnection();
		} catch (CoreException ce) {
			// TODO handle it
		}
	}

	private static void sendFolders(IPath[] iPaths) throws IOException, CoreException {
		sendInt(iPaths.length);
		for (int i = 0; i < iPaths.length; i++) {
			sendFolder(iPaths[i]);
		}
	}

	private static void sendFolder(IPath iPath) throws CoreException, IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFolder folder = root.getFolder(iPath);
		String folderName = folder.getName();
		sendString(folderName);
		IResource[] children = folder.members();
		List<IFolder> folders = new ArrayList<IFolder>();
		List<IFile> files = new ArrayList<IFile>();
		for (IResource resource : children) {
			if (resource instanceof IFolder) {
				folders.add((IFolder) resource);
			}
			else if (resource instanceof IFile) {
				files.add((IFile) resource);
			}
		}
	}

	private static void requestVerification(IPath filePath) throws IOException, CoreException, IncorrectPasswordException {
		// TODO make this not dependent on the current workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(filePath);
		String filename = file.getName();
		initializeConnection(2);
		sendString(filename);
		recvInt();
		IPath logPath = filePath.removeFileExtension().addFileExtension("log"); //$NON-NLS-1$
		IFile logFile = root.getFile(logPath);
		if (logFile.exists()) {
			// TODO fix null
			logFile.delete(true, null);
		}
		// TODO fix null
		logFile.create(is, true, null);
		closeConnection();
	}

	private static void sendFiles(IPath[] iPaths) throws IOException, CoreException {
		sendInt(iPaths.length);
		for (int i = 0; i < iPaths.length; i++) {
			sendFile(iPaths[i]);
		}
	}

	private static void sendFile(IPath iPath) throws CoreException, IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(iPath);
		sendFile(file);
	}

	private static void closeConnection() throws IOException {
		recvInt();
		os.close();
		is.close();
		socket.close();
		os = null;
		is = null;
		socket = null;
	}

	private static void initializeConnection(int instructionType) throws IOException, IncorrectPasswordException {
		int port = 8883;
		// TODO switch to DNS address of formal
		socket = new Socket("127.0.0.1", port);
		os = socket.getOutputStream();
		is = socket.getInputStream();

		// login
		String username, password;
		IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		username = preferenceStore.getString(GIGPreferencePage.USERNAME);
		password = preferenceStore.getString(GIGPreferencePage.PASSWORD);
		sendString(username);
		sendString(password);
		int i = recvInt();
		if (i == -1) {
			throw new IncorrectPasswordException();
		}
		sendInt(instructionType);
	}

	/*
	 * Builds the paths needed to run klee and gklee based on values from the Preferences
	 */
	private static void buildEnvPath(Map<String, String> env) {
		IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		env.put(GIGPreferencePage.GKLEE_HOME, preferenceStore.getString(GIGPreferencePage.GKLEE_HOME));
		env.put(GIGPreferencePage.FLA_KLEE_HOME_DIR, preferenceStore.getString(GIGPreferencePage.FLA_KLEE_HOME_DIR));
		StringBuilder sBuilder = new StringBuilder(env.get("PATH") + ':'); //$NON-NLS-1$
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.GKLEE_DEBUG_PLUS_ASSERTS_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.LLVM_DEBUG_PLUS_ASSERTS_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.LLVM_GCC_LINUX_BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.BIN) + ':');
		sBuilder.append(preferenceStore.getString(GIGPreferencePage.ADDITIONAL_PATH));
		String path = sBuilder.toString();
		env.put("PATH", path); //$NON-NLS-1$
	}

	/*
	 * Processes the binary created by klee
	 */
	private static void processBinary(IPath binaryPath) throws IOException, CoreException, InterruptedException {
		// setup the log file path
		IPath logPath = binaryPath.removeFileExtension().addFileExtension("log"); //$NON-NLS-1$

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		String binaryOSPath = workspaceRoot.getFile(binaryPath).getLocation().toOSString();
		IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();
		String gkleeOSPath = preferenceStore.getString(GIGPreferencePage.BIN) + "/gklee"; //$NON-NLS-1$
		ProcessBuilder processBuilder = new ProcessBuilder(gkleeOSPath, binaryOSPath);
		buildEnvPath(processBuilder.environment());

		Process process = processBuilder.start();
		process.waitFor();
		if (process.exitValue() != 0) {
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			Scanner scan = new Scanner(br);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				System.out.println(line);
			}
			processExitOnValue(process.exitValue());
			return;
		}
		InputStream is = process.getInputStream();
		IFile logFile = workspaceRoot.getFile(logPath);
		if (logFile.exists()) {
			// TODO fix null
			logFile.setContents(is, true, false, null);
		}
		else {
			// TODO fix null
			logFile.create(is, true, null);
		}
		is.close();

		// refresh eclipse environment to make it aware of the new log file
		IProject project = workspaceRoot.getFile(binaryPath).getProject();
		IFolder gigFolder = project.getFolder("gig"); //$NON-NLS-1$
		// TODO fix null
		gigFolder.refreshLocal(1, null);

		processLog(logPath);
	}

	private static void processExitOnValue(int exitValue) {
		// TODO Auto-generated method stub
		// print some sort of error to console/dialog
	}

	/*
	 * Processes a log file generated by gklee
	 */
	private static void processLog(IPath logPath) throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IFile logFile = workspaceRoot.getFile(logPath);
		InputStream logInputStream = logFile.getContents();
		try {
			final GkleeLog gkleeLog = new GkleeLog(logInputStream);
			UIJob job = new UIJob(Messages.UPDATE_GIG) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					GIGView.getDefault().update(gkleeLog);
					return Status.OK_STATUS;
				}
			};
			job.setPriority(UIJob.INTERACTIVE);
			job.schedule();
		} catch (IllegalStateException e) {
			StatusManager.getManager().handle(
					new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.PARSE_EXCEPTION, e));
		} catch (LogException e) {
			StatusManager.getManager().handle(
					new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.LOG_EXCEPTION_THREAD_BANK_CONFLICT));
		}
	}

	public static void startJob(Job job) {
		jobsLock.lock();
		try {
			if (jobState == JobState.None) {
				GIGUtilities.job = job;
				job.setPriority(Job.LONG);
				job.schedule();
				jobState = JobState.Running;
			}
		} finally {
			jobsLock.unlock();
		}
	}

	/*
	 * Used to correctly stop the job midway through for both canceling and exiting purposes
	 */
	public static void stopJob() {
		jobsLock.lock();
		if (jobState == JobState.Running) {
			jobState = JobState.Canceled;
			jobsLock.unlock();
			int i = 1000;
			/*
			 * We are waiting and giving the job time to cleanly exit itself, it will signal us by changing the jobState to
			 * None.
			 * Or we wait for timeout and kill it with a cancel
			 */
			while (jobState == JobState.Canceled && i > 0) {
				try {
					Thread.sleep(1);
					i--;
				} catch (InterruptedException e) {
					StatusManager.getManager().handle(
							new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.INTERRUPTED_EXCEPTION, e));
				}
			}
			if (i <= 0) {
				job.cancel();
				job = null;
				jobsLock.lock();
				jobState = JobState.None;
				jobsLock.unlock();
			}
		}
		else {
			jobsLock.unlock();
		}
	}

	protected static void sendInt(int i) throws IOException {
		byte[] buffer = new byte[4];
		buffer[0] = (byte) (i >> 24 & 0xff);
		buffer[1] = (byte) (i >> 16 & 0xff);
		buffer[2] = (byte) (i >> 8 & 0xff);
		buffer[3] = (byte) (i >> 0 & 0xff);
		os.write(buffer);
	}

	protected static void sendString(String string) throws IOException {
		sendInt(string.length());
		os.write(string.getBytes());
	}

	protected static int recvInt() throws IOException {
		int len = 4;
		int off = 0;
		byte[] buffer = new byte[4];
		while (off < len) {
			off += is.read(buffer, off, len - off);
		}
		int ret = 0;
		ret |= (buffer[0] << 24) & 0xff000000;
		ret |= (buffer[1] << 16) & 0x00ff0000;
		ret |= (buffer[2] << 8) & 0x0000ff00;
		ret |= (buffer[3] << 0) & 0x000000ff;
		return ret;
	}

	protected static String recvString() throws IOException {
		int len = recvInt();
		int off = 0;
		byte[] buffer = new byte[len];
		while (off < len) {
			off += is.read(buffer, off, len - off);
		}
		return new String(buffer);
	}

	/*
	 * This sends the folders and files to the server including their containing parent directories up to but not including the
	 * project
	 */
	public static void sendFoldersAndFiles(List<IFolder> folders, List<IFile> files) throws CoreException, IOException,
			IncorrectPasswordException {
		initializeConnection(0);
		ProjectToSend projectToSend = new ProjectToSend();
		for (IFolder folder : folders) {
			FolderToSend folderToSend = new FolderToSend(folder);
			projectToSend.add(folderToSend);
		}
		for (IFile file : files) {
			projectToSend.add(file);
		}
		projectToSend.send();
		closeConnection();
	}

	public static void sendFile(IFile file) throws IOException, CoreException {
		String filename = file.getName();
		sendString(filename);
		InputStream is = file.getContents();
		int len = is.available();
		sendInt(len);
		int off = 0;
		byte[] buffer = new byte[len];
		while (off < len) {
			int rec = is.read(buffer, off, len - off);
			os.write(buffer, off, rec);
			off += rec;
		}
		is.close();
	}

	public static ServerTreeItem getServerFoldersAndFilesRoot() throws IOException, IncorrectPasswordException {
		ServerTreeItem root = new ServerTreeItem("root"); //$NON-NLS-1$

		initializeConnection(1);
		int numFolders = recvInt();
		for (int i = 0; i < numFolders; i++) {
			recvFolderInfo(root);
		}
		int numFiles = recvInt();
		for (int i = 0; i < numFiles; i++) {
			recvFileInfo(root);
		}
		closeConnection();

		return root;
	}

	private static void recvFileInfo(ServerTreeItem root) throws IOException {
		String name = recvString();
		new ServerTreeItem(name, root, false);
	}

	private static void recvFolderInfo(ServerTreeItem root) throws IOException {
		String name = recvString();
		ServerTreeItem folder = new ServerTreeItem(name, root, true);
		int numFolders = recvInt();
		for (int i = 0; i < numFolders; i++) {
			recvFolderInfo(folder);
		}
		int numFiles = recvInt();
		for (int i = 0; i < numFiles; i++) {
			recvFileInfo(folder);
		}
	}

	public static void importFoldersAndFiles(IProject project, Object[] objects) throws IOException, IncorrectPasswordException,
			CoreException {
		ProjectToRecv projectToRecv = new ProjectToRecv();
		for (Object o : objects) {
			ServerTreeItem item = (ServerTreeItem) o;
			projectToRecv.add(item, true);
		}
		projectToRecv.sendNamesRecvData(project);
	}

	public static void sendNamesRecvData(IProject project, List<FolderToRecv> folders, List<String> files) throws IOException,
			IncorrectPasswordException, CoreException {
		initializeConnection(3);

		sendInt(folders.size());
		for (FolderToRecv folder : folders) {
			folder.sendNamesRecvData(project);
		}
		sendInt(files.size());
		for (String filename : files) {
			sendString(filename);
			IFile file = project.getFile(filename);
			int len = recvInt();
			byte[] buffer = new byte[len];
			int off = 0;
			while (off < len) {
				off += is.read(buffer, off, len - off);
			}
			InputStream inputStream = new ByteArrayInputStream(buffer);
			if (file.exists()) {
				// TODO fix null
				file.setContents(inputStream, true, true, null);
				inputStream.close();
			}
			else {
				// TODO fix null
				file.create(inputStream, true, null);
				inputStream.close();
			}
		}

		closeConnection();
		// TODO fix null
		project.refreshLocal(IProject.DEPTH_INFINITE, null);
	}

	public static void sendNamesRecvData(IContainer container, String name, List<FolderToRecv> folders, List<String> files)
			throws IOException, CoreException {
		sendString(name);
		IFolder folder;
		if (container instanceof IFolder) {
			folder = ((IFolder) container).getFolder(name);
		}
		else {
			folder = ((IProject) container).getFolder(name);
		}
		if (!folder.exists()) {
			// TODO fix null
			folder.create(true, true, null);
		}
		int numFolders = folders.size();
		sendInt(numFolders);
		for (int i = 0; i < numFolders; i++) {
			folders.get(i).sendNamesRecvData(folder);
		}
		int numFiles = files.size();
		sendInt(numFiles);
		for (int i = 0; i < numFiles; i++) {
			String filename = files.get(i);
			sendString(filename);
			IFile file = folder.getFile(filename);
			int len = recvInt();
			byte[] buffer = new byte[len];
			int off = 0;
			while (off < len) {
				off += is.read(buffer, off, len - off);
			}
			InputStream inputStream = new ByteArrayInputStream(buffer);
			if (file.exists()) {
				// TODO fix null
				file.setContents(inputStream, true, true, null);
				inputStream.close();
			}
			else {
				// TODO fix null
				file.create(inputStream, true, null);
				inputStream.close();
			}
		}
	}

	public static void deleteRemoteFiles(Object[] objects) throws IOException, IncorrectPasswordException {
		initializeConnection(4);

		sendInt(objects.length);
		for (Object o : objects) {
			ServerTreeItem item = (ServerTreeItem) o;
			sendString(item.getFullName());
		}

		closeConnection();
	}

	public static void remoteVerifyFile(IProject project, ServerTreeItem item) throws IOException, IncorrectPasswordException,
			CoreException {
		initializeConnection(2);

		String filePathString = item.getFullName();
		sendString(filePathString);
		String logString = recvString();
		IFile file = project.getFile(filePathString);
		IPath filePath = file.getProjectRelativePath();
		IPath logPath = filePath.removeFileExtension().addFileExtension("log"); //$NON-NLS-1$
		IFile logFile = project.getFile(logPath);
		InputStream logInputStream = new ByteArrayInputStream(logString.getBytes());
		if (logFile.exists()) {
			// TODO fix null
			logFile.setContents(logInputStream, true, false, null);
		}
		else {
			// TODO fix null
			logFile.create(logInputStream, true, null);
		}

		closeConnection();
	}
}