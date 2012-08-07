package org.eclipse.ptp.gig.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class FolderToSend {

	private final IFolder folder;
	protected final List<FolderToSend> foldersToSend = new ArrayList<FolderToSend>();
	protected final List<IFile> filesToSend = new ArrayList<IFile>();

	public FolderToSend(IFolder folder2) throws CoreException {
		IPath path = folder2.getFullPath();
		int segments = path.segmentCount();
		this.folder = folder2.getProject().getFolder(path.segment(1));
		if (segments > 2) {
			foldersToSend.add(new FolderToSend(folder, folder2, 2));
		}
		else {
			this.addAll();
		}
	}

	private FolderToSend(IFolder parentFolder, IFolder folder2, int i) throws CoreException {
		IPath path = folder2.getFullPath();
		int segments = path.segmentCount();
		this.folder = parentFolder.getFolder(path.segment(i));
		if (segments > i + 1) {
			foldersToSend.add(new FolderToSend(folder, folder2, i + 1));
		}
		else {
			this.addAll();
		}
	}

	private FolderToSend(IFolder folder, boolean addAll) throws CoreException {
		this.folder = folder;
		if (addAll) {
			this.addAll();
		}
	}

	public FolderToSend(IFile file) {
		IPath path = file.getFullPath();
		int segments = path.segmentCount();
		this.folder = file.getProject().getFolder(path.segment(1));
		if (segments > 3) {
			foldersToSend.add(new FolderToSend(folder, file, 2));
		}
		else {
			this.filesToSend.add(file);
		}
	}

	private FolderToSend(IFolder parentFolder, IFile file, int i) {
		IPath path = file.getFullPath();
		int segments = path.segmentCount();
		this.folder = parentFolder.getFolder(path.segment(i));
		if (segments > i + 2) {
			foldersToSend.add(new FolderToSend(folder, file, i + 1));
		}
		else {
			this.filesToSend.add(file);
		}
	}

	private void addAll() throws CoreException {
		IResource[] children = folder.members();
		for (IResource resource : children) {
			if (resource instanceof IFile) {
				filesToSend.add((IFile) resource);
			}
			else if (resource instanceof IFolder) {
				IFolder childFolder = (IFolder) resource;
				FolderToSend folderToSend = new FolderToSend(childFolder, true);
				foldersToSend.add(folderToSend);
			}
		}
	}

	public String getName() {
		return folder.getName();
	}

	public void add(List<FolderToSend> foldersToSend2, List<IFile> filesToSend2) {
		for (FolderToSend fts : foldersToSend2) {
			this.add(fts);
		}
		for (IFile file : filesToSend2) {
			this.add(file);
		}
	}

	private void add(IFile file) {
		for (IFile curr : filesToSend) {
			if (curr.getName().equals(file.getName())) {
				return;
			}
		}
		filesToSend.add(file);
	}

	private void add(FolderToSend fts) {
		for (FolderToSend fts2 : foldersToSend) {
			if (fts.getName().equals(fts2.getName())) {
				fts2.add(fts.foldersToSend, fts.filesToSend);
				return;
			}
		}
		foldersToSend.add(fts);
	}

	@Override
	public String toString() {
		return folder.toString();
	}

	public void send() throws IOException, CoreException {
		GIGUtilities.sendString(folder.getName());
		GIGUtilities.sendInt(foldersToSend.size());
		for (FolderToSend fts : foldersToSend) {
			fts.send();
		}
		GIGUtilities.sendInt(filesToSend.size());
		for (IFile file : filesToSend) {
			GIGUtilities.sendFile(file);
		}
	}
}
