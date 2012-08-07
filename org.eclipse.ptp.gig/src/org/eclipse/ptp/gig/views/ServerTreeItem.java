package org.eclipse.ptp.gig.views;

import java.util.ArrayList;
import java.util.List;

public class ServerTreeItem {

	private final List<ServerTreeItem> children = new ArrayList<ServerTreeItem>();
	private final ServerTreeItem parent;
	private final String name;
	private final boolean isFolder;

	public ServerTreeItem(String name) {
		this.name = name;
		this.parent = null;
		this.isFolder = true;
	}

	public ServerTreeItem(String name, ServerTreeItem parent, boolean isFolder) {
		this.name = name;
		this.parent = parent;
		parent.add(this);
		this.isFolder = isFolder;
	}

	private void add(ServerTreeItem serverTreeItem) {
		children.add(serverTreeItem);
	}

	public String getName() {
		return name;
	}

	public Object[] getChildren() {
		return children.toArray();
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public ServerTreeItem getParent() {
		return parent;
	}

	public boolean isFolder() {
		return isFolder;
	}

	/*
	 * returns the relative path of this
	 */
	public String getFullName() {
		if (parent.name.equals("root")) { //$NON-NLS-1$
			return name; //$NON-NLS-1$
		}
		// we want the linux file separator, since this is the one that the server will use
		return parent.getFullName() + '/' + name;
	}

}