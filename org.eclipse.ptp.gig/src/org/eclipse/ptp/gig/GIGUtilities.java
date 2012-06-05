package org.eclipse.ptp.gig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.preferences.GIGPreferencePage;

public class GIGUtilities {

	public static void processSource(IPath filePath) throws IOException, CoreException, InterruptedException {
		// TODO enforce that the file is of the right type

		// make the directory if necessary
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getFile(filePath).getProject();
		IFolder folder = project.getFolder("gig"); //$NON-NLS-1$
		if (!folder.exists()) {
			// TODO fix null
			folder.create(true, true, null);
		}

		// now prepare the executable
		IPath folderPath = folder.getFullPath();
		int segments = filePath.segmentCount();
		IPath filename = filePath.removeFirstSegments(segments - 1).removeFileExtension();
		filename = folderPath.append(filename);
		IPath exec = filename.addFileExtension("gig"); //$NON-NLS-1$
		filename = filename.addFileExtension("C"); //$NON-NLS-1$

		// copy the file over to the gig folder
		IFile origFile = (IFile) root.findMember(filePath);
		IResource iRes = root.findMember(filename);
		if (iRes != null && iRes.exists()) {
			// TODO fix null
			iRes.delete(true, null);
		}
		// TODO fix null's
		origFile.copy(filename, true, null);
		root.findMember(folderPath).refreshLocal(1, null);

		// begin building the command line
		String osStringSrc, osStringBinary;
		IPath absSrcLoc = root.findMember(filename).getLocation();
		osStringSrc = absSrcLoc.toOSString();
		osStringBinary = absSrcLoc.removeFileExtension().addFileExtension("gig").toOSString(); //$NON-NLS-1$

		IPreferenceStore pstore = GIGPlugin.getDefault().getPreferenceStore();
		String klee_value = pstore.getString(GIGPreferencePage.bin) + "/klee-l++"; //$NON-NLS-1$
		ProcessBuilder pb = new ProcessBuilder(klee_value, osStringSrc, "-o", osStringBinary); //$NON-NLS-1$
		Map<String, String> env = pb.environment();

		buildEnvPath(env);

		// start process and print out its output
		// TODO have the output go to console (GIG Console?)
		Process process = pb.start();
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

		// refresh environment
		// TODO fix null
		folder.refreshLocal(1, null);

		// now process the executable
		processExec(exec);
	}

	/*
	 * Builds the paths needed to run klee and gklee based on values from the Preferences
	 */
	private static void buildEnvPath(Map<String, String> env) {
		IPreferenceStore pstore = GIGPlugin.getDefault().getPreferenceStore();
		String GKLEE_HOME_value = pstore.getString(GIGPreferencePage.GKLEE_HOME);
		env.put(GIGPreferencePage.GKLEE_HOME, GKLEE_HOME_value);
		String FLA_KLEE_HOME_DIR_value = pstore.getString(GIGPreferencePage.FLA_KLEE_HOME_DIR);
		env.put(GIGPreferencePage.FLA_KLEE_HOME_DIR, FLA_KLEE_HOME_DIR_value);
		StringBuilder sBuilder = new StringBuilder(env.get("PATH") + ':'); //$NON-NLS-1$
		sBuilder.append(pstore.getString(GIGPreferencePage.Gklee_DebugPlusAsserts_bin) + ':');
		sBuilder.append(pstore.getString(GIGPreferencePage.llvm_DebugPlusAsserts_bin) + ':');
		sBuilder.append(pstore.getString(GIGPreferencePage.llvm_gcc_linux_bin) + ':');
		sBuilder.append(pstore.getString(GIGPreferencePage.bin) + ':');
		sBuilder.append(pstore.getString(GIGPreferencePage.other_PATH));
		String PATH_value = sBuilder.toString();
		env.put("PATH", PATH_value); //$NON-NLS-1$
	}

	private static void processExec(IPath execPath) throws IOException, CoreException, InterruptedException {
		// TODO enforce that the file is of the right type

		// setup the log file path
		IPath logPath = execPath.addFileExtension("log"); //$NON-NLS-1$

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		String osBinary;
		osBinary = root.getFile(execPath).getLocation().toOSString();

		IPreferenceStore pstore = GIGPlugin.getDefault().getPreferenceStore();
		String gklee_value = pstore.getString(GIGPreferencePage.bin) + "/gklee"; //$NON-NLS-1$
		ProcessBuilder pb = new ProcessBuilder(gklee_value, osBinary);
		buildEnvPath(pb.environment());

		// run it, wait for it, then send the output to the log file
		Process process = pb.start();
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
		IFile logFile = root.getFile(logPath);
		if (logFile.exists()) {
			// TODO fix null
			logFile.setContents(is, true, false, null);
		}
		else {
			// TODO fix null
			logFile.create(is, true, null);
		}
		is.close();

		// refresh eclipse environment
		IProject project = root.getFile(execPath).getProject();
		IFolder folder = project.getFolder("gig"); //$NON-NLS-1$
		// TODO fix null
		folder.refreshLocal(1, null);

		// now process the log file
		processLog(logPath);
	}

	private static void processExitOnValue(int exitValue) {
		// TODO Auto-generated method stub
		// print some sort of error to console
	}

	private static void processLog(IPath logFile) throws IOException, CoreException {
		// TODO enforce that the file is of the right type
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile file = root.getFile(logFile);
		processContents(file.getContents());
	}

	private static void processContents(InputStream contents) throws IOException {
		// TODO Auto-generated method stub

		contents.close();
	}
}
