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
		if (parent.parent == null) {
			return name;
		}
		// we want the linux file separator, since this is the one that the server will use
		return parent.getFullName() + '/' + name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ServerTreeItem) {
			ServerTreeItem item = (ServerTreeItem) o;
			if (this.parent == null) {
				if (item.parent == null) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				if (item.parent == null) {
					return false;
				}
				else {
					return this.name.equals(item.name) && this.parent.equals(item.parent);
				}
			}
		}
		return false;
	}

}