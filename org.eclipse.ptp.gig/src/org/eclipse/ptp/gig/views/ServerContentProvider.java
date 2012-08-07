package org.eclipse.ptp.gig.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ServerContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ServerTreeItem) {
			ServerTreeItem item = (ServerTreeItem) inputElement;
			return item.getChildren();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ServerTreeItem) {
			ServerTreeItem item = (ServerTreeItem) parentElement;
			return item.getChildren();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ServerTreeItem) {
			ServerTreeItem item = (ServerTreeItem) element;
			return item.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ServerTreeItem) {
			ServerTreeItem item = (ServerTreeItem) element;
			return item.hasChildren();
		}
		return false;
	}

}
