package org.eclipse.ptp.gig.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.GIGUtilities;
import org.eclipse.ptp.gig.GIGUtilities.JobState;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.views.GIGView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

public class ToolbarHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorInput input = HandlerUtil.getActiveEditorInputChecked(event);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			final IPath filePath = file.getFullPath();

			final IWorkbench wb = PlatformUI.getWorkbench();
			final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
			try {
				page.showView(GIGView.ID);
			} catch (PartInitException e) {
				StatusManager.getManager().handle(
						new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.PART_INIT_EXCEPTION, e));
			}

			// start it in a new thread so as to not block UI thread
			Job job = new Job(Messages.RUN_GKLEE) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						// TODO add a return type or something so that we can check if OK or cancelled
						GIGUtilities.processSource(filePath);
						GIGUtilities.setJobState(JobState.None);
						return Status.OK_STATUS;
					} catch (IOException e) {
						StatusManager.getManager().handle(new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
					} catch (CoreException e) {
						StatusManager.getManager().handle(e, GIGPlugin.PLUGIN_ID);
					} catch (InterruptedException e) {
						StatusManager.getManager().handle(
								new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.INTERRUPTED_EXCEPTION, e));
					}
					finally {
						GIGUtilities.setJobState(JobState.None);
					}
					return Status.CANCEL_STATUS;
				}

			};

			GIGUtilities.startJob(job);

			// (new Thread() {
			// @Override
			// public void run() {
			// try {
			// GIGUtilities.processSource(filePath);
			// } catch (IOException e) {
			// StatusManager.getManager().handle(new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.IOException, e));
			// } catch (CoreException e) {
			// StatusManager.getManager().handle(e, GIGPlugin.PLUGIN_ID);
			// } catch (InterruptedException e) {
			// StatusManager.getManager().handle(
			// new Status(Status.ERROR, GIGPlugin.PLUGIN_ID, Messages.InterruptedException, e));
			// }
			// }
			// }).start();
		}

		return null;
	}
}
