package org.eclipse.ptp.gig.log;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.gig.messages.Messages;

public class ThreadInfo {
	private static Map<String, IFile> fileResolver = new HashMap<String, IFile>();
	private final String block, thread, filename, line;
	private Integer blockInt, threadInt, lineInt;
	private IFile file;

	public ThreadInfo(String block, String thread, String file, String line) {
		this.block = block;
		this.thread = thread;
		this.filename = file;
		this.line = line;
	}

	/*
	 * This is very likely to throw a NumberFormatException
	 */
	public int getBlock() {
		if (blockInt == null) {
			return blockInt = Integer.parseInt(block);
		}
		return blockInt;
	}

	/*
	 * This is very likely to throw a NumberFormatException
	 */
	public int getThread() {
		if (threadInt == null) {
			return threadInt = Integer.parseInt(thread);
		}
		return threadInt;
	}

	public String getLabel(IProject project) {
		return String.format(Messages.THREAD_INFO_FORMAT, getBlock(), getThread(), getLine(), getFilename(project));
	}

	private String getFilename(IProject project) {
		if (file == null) {
			file = getFile(project);
			if (file != null) {
				return file.getName();
			}
			else {
				return filename;
			}
		}
		return file.getName();
	}

	private IFile getFile(IProject project) {
		// TODO need to find a better way of arbitrating server vs client resources
		if (fileResolver.containsKey(filename)) {
			return fileResolver.get(filename);
		}
		String serverHomeDirectory = "/home/bgibson/GIGServer/"; //$NON-NLS-1$
		if (filename.startsWith(serverHomeDirectory)) {
			String filePath = filename.substring(serverHomeDirectory.length());
			filePath = filePath.substring(filePath.indexOf('/') + 1);
			IFile ret = getFile(project, filePath);
			fileResolver.put(filename, ret);
			return ret;
		}
		return null;
	}

	private IFile getFile(IContainer container, String filePath) {
		int splitPoint = filePath.indexOf('/');
		if (splitPoint == -1) {
			if (container instanceof IProject) {
				IProject project = (IProject) container;
				return project.getFile(filePath);
			}
			else if (container instanceof IFolder) {
				IFolder folder = (IFolder) container;
				return folder.getFile(filePath);
			}
		}
		String folderName = filePath.substring(0, splitPoint);
		filePath = filePath.substring(splitPoint + 1);
		if (container instanceof IProject) {
			IProject project = (IProject) container;
			IFolder folder = project.getFolder(folderName);
			return getFile(folder, filePath);
		}
		else if (container instanceof IFolder) {
			IFolder parent = (IFolder) container;
			IFolder folder = parent.getFolder(folderName);
			return getFile(folder, filePath);
		}
		return null;
	}

	public int getLine() {
		if (lineInt == null) {
			return lineInt = Integer.parseInt(line);
		}
		return lineInt;
	}

	public IFile getFile() {
		return file;
	}

	public static void reset() {
		fileResolver.clear();
	}
}
