package org.eclipse.ptp.gig.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ServerTreeItemSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object a, Object b) {
		ServerTreeItem item1 = (ServerTreeItem) a;
		ServerTreeItem item2 = (ServerTreeItem) b;
		return item1.getName().compareTo(item2.getName());
	}

	@Override
	public int category(Object a) {
		ServerTreeItem item = (ServerTreeItem) a;
		if (item.isFolder()) {
			return 1;
		}
		else {
			return 2;
		}
	}
}
