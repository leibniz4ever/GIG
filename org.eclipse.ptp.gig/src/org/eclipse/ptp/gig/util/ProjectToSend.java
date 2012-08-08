package org.eclipse.ptp.gig.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class ProjectToSend {
	private final List<FolderToSend> foldersToSend = new ArrayList<FolderToSend>();
	private final List<IFile> filesToSend = new ArrayList<IFile>();

	/*
	 * Argument is a folder to be added to the project, if already there, merge
	 */
	public void add(FolderToSend folderToSend) {
		for (FolderToSend curr : foldersToSend) {
			if (curr.getName().equals(folderToSend.getName())) {
				curr.add(folderToSend.foldersToSend, folderToSend.filesToSend);
				return;
			}
		}
		foldersToSend.add(folderToSend);
	}

	public void send() throws IOException, CoreException {
		GIGUtilities.sendInt(foldersToSend.size());
		for (FolderToSend fts : foldersToSend) {
			fts.send();
		}
		GIGUtilities.sendInt(filesToSend.size());
		for (IFile file : filesToSend) {
			GIGUtilities.sendFile(file);
		}
	}

	public void add(IFile file) {
		if (file.getFullPath().segmentCount() > 2) {
			FolderToSend fts = new FolderToSend(file);
			this.add(fts);
		}
		else {
			this.filesToSend.add(file);
		}
	}

}