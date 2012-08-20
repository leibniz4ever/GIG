

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ServerThread extends Thread {

	private static String masterFolderPath;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private static String GKLEE_BIN;
	private static String LLVM_GCC_BIN;
	private static String LLVM_DA_BIN;
	private static String GKLEE_DA_BIN;
	private static String GKLEE_HOME;
	static {
		switch(1) {
		case 0: //bgibson
			GKLEE_BIN = "/home/bgibson/gklee/bin";
			LLVM_GCC_BIN = "/home/bgibson/gklee/llvm-gcc4.2-2.8-x86_64-linux/bin";
			LLVM_DA_BIN = "/home/bgibson/gklee/llvm-2.8/Debug+Asserts/bin";
			GKLEE_DA_BIN = "/home/bgibson/gklee/Gklee/Debug+Asserts/bin";
			GKLEE_HOME = "/home/bgibson/gklee";
			
			masterFolderPath = "/home/bgibson/GIGServer/";
		break;
		case 1: //formal
			GKLEE_BIN = "/home/tomcat/gklee/bin";
			LLVM_GCC_BIN = "/home/tomcat/gklee/llvm-gcc-4.2-2.8/bin";
			LLVM_DA_BIN = "/home/tomcat/gklee/llvm-2.8/Debug/bin";
			GKLEE_DA_BIN = "/home/tomcat/gklee/Gklee/Debug/bin";
			GKLEE_HOME = "/home/tomcat/gklee";
			
			masterFolderPath = "/home/bgibson/GIGServer/";
			break;
		}
	}
	private static final String GKLEE = "gklee";
	public static final int WRONG_PASSWORD = -1, ILLEGAL_COMMAND = -2, IOEXCEPTION = -3, INTERRUPTED_EXCEPTION = -4;

	public static void main(String[] args) {
		GIGServer.main(args);
	}
	
	public ServerThread(Socket socket) throws IOException {
//		killerThread = new KillerThread();
//		killerThread.start();
		this.socket = socket;
		this.is = socket.getInputStream();
		this.os = socket.getOutputStream();
	}
	
	public void run() {
		try {
			System.out.println("Starting ServerThread");
			String username = recvString();
			String password = recvString();
			
			File folder = new File(masterFolderPath + username);
			File passwordFile = new File(folder.getAbsolutePath() + File.separatorChar + "password.txt");
			if(!folder.exists()) {
				folder.mkdirs();
				FileOutputStream fos = new FileOutputStream(passwordFile);
				fos.write(password.getBytes());
				fos.close();
			}
			
			System.out.println("Checking password");
			
			if(!checkPassword(passwordFile, password)) {
				System.out.println("Password Failed!");
				sendInt(WRONG_PASSWORD);
				return;
			}
			else {
				sendInt(0);
			}

			int instruction = recvInt();
			System.out.printf("Request from user %s with command %d\n", username, instruction);
			switch(instruction) {
			case 0:
				//the default user, gklee, cannot have files added to it
				if(username.equals(GKLEE)) {
					System.out.println("Illegal command on gklee user");
					sendInt(ILLEGAL_COMMAND);
					break;
				}
				else {
					sendInt(0);
				}

				System.out.println("Server is receiving Files and Folders");
				//receive folders and files
				int numFolders = recvInt();
				for(int i = 0; i<numFolders; i++) {
					recvFolder(folder);
				}
				int numFiles = recvInt();
				for(int i = 0; i<numFiles; i++) {
					recvFile(folder);
				}
				sendInt(0);
				break;
			case 1:
				//This is servicing a request for the folders and file structural info
				System.out.println("Server is sending the folder structure");
				File[] children = folder.listFiles();
				List<File> folders = new ArrayList<File>();
				List<File> files = new ArrayList<File>();
				for(File file : children) {
					if(file.isFile()) {
						files.add(file);
					}
					else {
						folders.add(file);
					}
				}
				sendInt(folders.size());
				for(File file : folders) {
					sendFolderInfo(file);
				}
				sendInt(files.size()-1);
				for(File file : files) {
					String name = file.getName();
					if(!name.equals("password.txt"))
						sendString(name);
				}
				sendInt(0);
				break;
			case 2:
				//formally verify selected file
				String path = recvString();
				path = fixFileExtension(path);
				System.out.println("Server is formally verifying " + path);
				
				//now verify the file specified in path
				String log = processFile(masterFolderPath + username + File.separator + path);
				
				System.out.println("Sending log back");
				//now send back the log file
				if(log == null) {
					sendInt(INTERRUPTED_EXCEPTION);
				}
				else {
					sendString(log);
					sendInt(0);
				}
				break;
			case 3:
				//return specific folders and files
				System.out.println("Server is sending folders' and files' data to client");
				numFolders = recvInt();
				for(int i = 0; i<numFolders; i++) {
					recvNameSendFolder(folder);
				}
				numFiles = recvInt();
				for(int i = 0; i<numFiles; i++) {
					recvNameSendFile(folder);
				}
				sendInt(0);
				break;
			case 4:
				//delete specified folders and files
				System.out.println("Server is deleting");
				if(username.equals(GKLEE)) {
					System.out.println("Illegal command on gklee user");
					sendInt(ILLEGAL_COMMAND);
					break;
				}
				else {
					sendInt(0);
				}
				numFiles = recvInt();
				for(int i = 0; i<numFiles; i++) {
					String filePath = recvString();
					System.out.println("Deleting " + filePath);
					deleteFile(folder, filePath);
				}
				sendInt(0);
				break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			try {
				sendInt(IOEXCEPTION);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		finally {
			try {
				System.out.println("Done");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteFile(File folder, String fileString) {
		if(fileString.contains("/")) {
			String fileRoot = fileString.substring(0, fileString.indexOf('/'));
			fileString = fileString.substring(fileString.indexOf('/')+1, fileString.length());
			String newFolderPath = folder.getAbsolutePath() + File.separator + fileRoot;
			File newFolder = new File(newFolderPath);
			deleteFile(newFolder, fileString);
		}
		else {
			String filePath = folder.getAbsolutePath() + File.separator + fileString;
			File file = new File(filePath);
			if(file.exists()) {
				if(file.isFile()) {
					System.out.println("Deleting file " + file.getAbsolutePath());
					file.delete();
				}
				else {
					recursiveDelete(file);
				}
			}
		}
	}

	/*
	 * Deletes this file, and if a directory, deletes all contents of the directory
	 */
	private void recursiveDelete(File file) {
		if(file.isFile()) {
			System.out.println("Deleting file " + file.getAbsolutePath());
			file.delete();
			return;
		}
		File[] files = file.listFiles();
		if(files != null) {
			for(File curr : files) {
				recursiveDelete(curr);
			}
		}
		System.out.println("Deleting folder " + file.getAbsolutePath());
		file.delete();
	}

	private void recvNameSendFile(File parentFolder) throws IOException {
		String name = recvString();
		File file = new File(parentFolder.getAbsolutePath() + File.separator + name);
		FileInputStream fis = new FileInputStream(file);
		int len = fis.available();
		sendInt(len);
		int off = 0;
		byte[] buffer = new byte[len];
		System.out.println("Sending File: " + file.getAbsolutePath());
		while(off < len) {
			int rec = fis.read(buffer, off, len-off);
			os.write(buffer, off, len-off);
			off += rec;
		}
		fis.close();
	}

	private void recvNameSendFolder(File parentFolder) throws IOException {
		String name = recvString();
		File folder = new File(parentFolder.getAbsolutePath() + File.separator + name);
		System.out.println("Sending folder: " + folder.getAbsolutePath());
		int numFolders = recvInt();
		for(int i = 0; i<numFolders; i++) {
			recvNameSendFolder(folder);
		}
		int numFiles = recvInt();
		for(int i = 0; i<numFiles; i++) {
			recvNameSendFile(folder);
		}
	}

	private void sendFolderInfo(File parentFolder) throws IOException {
		sendString(parentFolder.getName());
		File[] children = parentFolder.listFiles();
		if(children == null) {
			sendInt(0);
			sendInt(0);
			return;
		}
		List<File> folders = new ArrayList<File>();
		List<File> files = new ArrayList<File>();
		for(File file : children) {
			if(file.isFile()) {
				files.add(file);
			}
			else {
				folders.add(file);
			}
		}
		sendInt(folders.size());
		for(File folder : folders) {
			sendFolderInfo(folder);
		}
		sendInt(files.size());
		for(File file : files) {
			sendString(file.getName());
		}
	}

	//[/home/bgibson/gklee/bin/klee-l++, /home/bgibson/EclipseWorkspaces/CUDA/CUDA/gig/shared_bitmap_buggy_GKLEE.C, -o, /home/bgibson/EclipseWorkspaces/CUDA/CUDA/gig/shared_bitmap_buggy_GKLEE.gig]
	//[/home/bgibson/gklee/bin/klee-l++, /home/bgibson/GIGServer/bgibson/src/CDExs/dot_buggy_CudaByExampleP88.C, -o, /home/bgibson/GIGServer/bgibson/src/CDExs/dot_buggy_CudaByExampleP88.gig]
	private String processFile(String sourcePath) throws IOException {
		String binaryPath = sourcePath.substring(0, sourcePath.lastIndexOf('.')) + ".gig";
		if(binaryPath.equals(sourcePath)) {
			processBinary(binaryPath);
		}
		ProcessBuilder processBuilder = new ProcessBuilder(GKLEE_BIN + "/klee-l++", sourcePath, "-o", binaryPath);
		Map<String, String> env = processBuilder.environment();
		setupEnv(env);
		System.out.println("Verifying " + sourcePath);
		Process process = processBuilder.start();
		logStream(process.getInputStream());
		try {
			process.waitFor();
			int i = process.exitValue();
			if(i != 0) {
				InputStream errorStream = process.getErrorStream();
				int len = errorStream.available();
				int off = 0;
				byte[] buf = new byte[len];
				while(off < len) {
					off += errorStream.read(buf, off, len-off);
				}
				return new String(buf);
			}
			else {
				String ret = processBinary(binaryPath);
				if(ret != null) {
					return ret;
				}
				else {
					InputStream errorStream = process.getErrorStream();
					int len = errorStream.available();
					int off = 0;
					byte[] buf = new byte[len];
					while(off < len) {
						off += errorStream.read(buf, off, len-off);
					}
					return new String(buf);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String processBinary(String binaryPath) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(GKLEE_BIN + "/gklee", "-emacs", binaryPath);
		Map<String, String> env = processBuilder.environment();
		setupEnv(env);
		System.out.println("Verifying " + binaryPath);
		Process process = processBuilder.start();
		try {
			InputStream logStream = process.getInputStream();
			InputStreamReader logStreamReader = new InputStreamReader(logStream);
			BufferedReader bufferedLogReader = new BufferedReader(logStreamReader);
			StringBuilder logStringBuilder = new StringBuilder();
			for(String line = bufferedLogReader.readLine(); line != null; line = bufferedLogReader.readLine()) {
				logStringBuilder.append(line);
				logStringBuilder.append('\n');
			}
			process.waitFor();
			int i = process.exitValue();
			if( i != 0) {
				InputStream errorStream = process.getErrorStream();
				int len = errorStream.available();
				int off = 0;
				byte[] buf = new byte[len];
				while(off < len) {
					off += errorStream.read(buf, off, len-off);
				}
				return new String(buf);
			}
			else {
				return logStringBuilder.toString();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			cleanUpKlee(binaryPath.substring(0, binaryPath.lastIndexOf('/')));
		}
	}

	private void cleanUpKlee(String substring) {
		File folder = new File(substring);
		String[] files = folder.list();
		for(String filename: files) {
			if(filename.startsWith("klee-")) {
				File file = new File(substring + File.separator + filename);
				recursiveDelete(file);
			}
		}
//		ProcessBuilder processBuilder = new ProcessBuilder("rm", "-r", substring + "/klee-*");
//		System.out.println("Removing with: rm -r " + substring + "/klee-*");
//		Process process = processBuilder.start();
//		InputStream stream = process.getInputStream();
//		Scanner scan = new Scanner(stream);
//		while(scan.hasNextLine()) {
//			scan.nextLine();
//		}
//		process.waitFor();
	}

	private void logStream(InputStream stream) throws IOException {
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);
		String string = new String(buffer);
		System.out.println(string);
	}

	private void setupEnv(Map<String, String> env) {
		env.put("GKLEE_HOME", GKLEE_HOME);
		env.put("FLA_KLEE_HOME_DIR", GKLEE_HOME);
		StringBuilder sBuilder = new StringBuilder(env.get("PATH") + ':'); //$NON-NLS-1$
		sBuilder.append(GKLEE_DA_BIN  + ':');
		sBuilder.append(LLVM_DA_BIN  + ':');
		sBuilder.append(LLVM_GCC_BIN  + ':');
		sBuilder.append(GKLEE_BIN );
		String path = sBuilder.toString();
		env.put("PATH", path); //$NON-NLS-1$
	}

	private boolean checkPassword(File passwordFile, String password) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(passwordFile);
		Scanner scanner = new Scanner(fis);
		if(!scanner.hasNext()) {
			//indicates no password
			return true;
		}
		String next = scanner.next();
		boolean ret = next.equals(password);
		return ret;
	}

	private void recvFolder(File parentFolder) throws IOException {
		String folderName = recvString();
		File folder = new File(parentFolder + File.separator + folderName);
		System.out.println("Receiving Folder " + folder.getAbsolutePath());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		int numFolders = recvInt();
		for(int i = 0; i<numFolders; i++) {
			recvFolder(folder);
		}
		int numFiles = recvInt();
		for(int i = 0; i<numFiles; i++) {
			recvFile(folder);
		}
	}

	private void recvFile(File folder) throws IOException {
		String filename = recvString();
		filename = fixFileExtension(filename);
		File file = new File(folder.getAbsolutePath() + File.separator + filename);
		System.out.println("Receiving File " + file.getAbsolutePath());
		if(file.exists()) {
			file.delete();
		}
		FileOutputStream fos = new FileOutputStream(file);
		int fileLength = recvInt();
		byte[] buffer = new byte[fileLength];
		int off = 0;
		while(off < fileLength) {
			int rec = is.read(buffer, off, fileLength-off);
			fos.write(buffer, off, rec);
			off += rec;
		}
		fos.close();
	}

	private String fixFileExtension(String filename) {
		String ext = filename.substring(filename.indexOf('.'));
		//Klee complains about .cu files
		if(ext.equals(".cu")) {
			ext = ".C";
			filename = filename.substring(0, filename.indexOf('.')) + ext;
		}
		return filename;
	}

	private void sendInt(int i) throws IOException {
		byte[] buffer = new byte[4];
		buffer[0] = (byte) (i>>24 & 0xff);
		buffer[1] = (byte) (i>>16 & 0xff);
		buffer[2] = (byte) (i>>8 & 0xff);
		buffer[3] = (byte) (i>>0 & 0xff);
		os.write(buffer);
	}
	
	private void sendString(String string) throws IOException {
		sendInt(string.length());
		os.write(string.getBytes());
	}
	
	private int recvInt() throws IOException {
		int len = 4;
		int off = 0;
		byte[] buffer = new byte[4];
		while(off < len) {
			off+=is.read(buffer, off, len-off);
		}
		int ret = 0;
		ret |= (buffer[0]<<24) & 0xff000000;
		ret |= (buffer[1]<<16) & 0x00ff0000;
		ret |= (buffer[2]<<8) & 0x0000ff00;
		ret |= (buffer[3]<<0) & 0x000000ff;
		return ret;
	}

	private String recvString() throws IOException {
		int len = recvInt();
		int off = 0;
		byte[] buffer = new byte[len];
		while(off < len) {
			off+=is.read(buffer, off, len-off);
		}
		return new String(buffer);
	}
}
