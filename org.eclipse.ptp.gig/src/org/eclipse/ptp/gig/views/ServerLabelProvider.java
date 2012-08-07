package org.eclipse.ptp.gig.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.swt.graphics.Image;

public class ServerLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object object) {
		final ServerTreeItem item = (ServerTreeItem) object;
		if (item.isFolder()) {
			return GIGPlugin.getImageDescriptor("icons/fldr_obj.gif").createImage(); //$NON-NLS-1$
		}
		return GIGPlugin.getImageDescriptor("icons/file_obj.gif").createImage(); //$NON-NLS-1$
	}

	@Override
	public String getText(Object element) {
		final ServerTreeItem item = (ServerTreeItem) element;
		return item.getName();
	}
}