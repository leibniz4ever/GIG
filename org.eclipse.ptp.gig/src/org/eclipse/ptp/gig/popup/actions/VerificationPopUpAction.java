package org.eclipse.ptp.gig.popup.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.GIGUtilities;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;

public class VerificationPopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	@Override
	public void run(IAction action) {
		Object[] oa = selection.toArray();
		if (oa.length != 0) {
			Object o = oa[0];
			IFile file = (IFile) o;
			final IPath filePath = file.getFullPath();

			// start it in a new thread so as to free the UI
			(new Thread() {
				@Override
				public void run() {
					try {
						GIGUtilities.processSource(filePath);
					} catch (IOException e) {
						StatusManager.getManager().handle(new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.IOException, e));
					} catch (CoreException e) {
						StatusManager.getManager().handle(e, GIGPlugin.PLUGIN_ID);
					} catch (InterruptedException e) {
						StatusManager.getManager().handle(
								new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.InterruptedException, e));
					}
				}
			}).start();
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
