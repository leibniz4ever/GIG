package org.eclipse.ptp.gig.popup.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.gig.GIGUtilities;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class VerificationPopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	@Override
	public void run(IAction action) {
		Object[] oa = selection.toArray();
		if (oa.length != 0) {
			Object o = oa[0];
			IFile file = (IFile) o;
			IPath filePath = file.getFullPath();
			try {
				GIGUtilities.processSource(filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		} else {
			this.selection = null;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
