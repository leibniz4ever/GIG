/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.views;

import java.util.List;

import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.log.GkleeLog;
import org.eclipse.ptp.gig.log.MemoryCoalescingByWholeWarp;
import org.eclipse.ptp.gig.log.ThreadBankConflict;
import org.eclipse.ptp.gig.log.WarpDivergence;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

public class GIGView extends ViewPart {

	public static final String ID = "org.eclipse.ptp.gig.views.GIGView"; //$NON-NLS-1$
	private static GIGView view;
	private Tree memoryCoalescingTree, bankConflictTree, warpDivergenceTree, deadlockTree;
	private CTabItem memoryCoalescingTab, bankConflictTab, warpDivergenceTab, deadlockTab;

	private CTabFolder cTabFolder;

	public GIGView() {
		super();
		view = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginHeight = 10;
		parentLayout.marginWidth = 10;
		parentLayout.numColumns = 1;

		cTabFolder = new CTabFolder(parent, SWT.TOP);

		reset();
	}

	private void reset() {
		CTabItem[] cTabItems = cTabFolder.getItems();
		for (int i = 0; i < cTabItems.length; i++) {
			cTabItems[i].dispose();
		}

		CTabItem cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, 0);
		cTabItem.setText(Messages.MEMORY_COALESCING);
		this.memoryCoalescingTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.memoryCoalescingTree);
		this.memoryCoalescingTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, 1);
		cTabItem.setText(Messages.BANK_CONFLICTS);
		this.bankConflictTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.bankConflictTree);
		this.bankConflictTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, 2);
		cTabItem.setText(Messages.WARP_DIVERGENCE);
		this.warpDivergenceTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.warpDivergenceTree);
		this.warpDivergenceTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, 3);
		cTabItem.setText(Messages.DEADLOCKS);
		this.deadlockTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.deadlockTree);
		this.deadlockTab = cTabItem;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static GIGView getDefault() {
		return view;
	}

	/*
	 * Takes a GkleeLog and updates this views components based on it.
	 * Run this only on thread with UI access
	 */
	public void update(GkleeLog gkleeLog) {
		reset();
		updateMemoryCoalescing(gkleeLog);
		updateBankConflicts(gkleeLog);
		updateWarpDivergence(gkleeLog);
		updateDeadlocks(gkleeLog);
	}

	private void updateMemoryCoalescing(GkleeLog gkleeLog) {
		this.memoryCoalescingTree.clearAll(true);
		Image image;
		// TODO change constant to preference
		if (gkleeLog.aveWarpMemoryCoalescingRate > 50) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.memoryCoalescingTab.setImage(image);
		TreeItem treeItem, subTreeItem;
		treeItem = new TreeItem(this.memoryCoalescingTree, SWT.NONE);
		treeItem.setText(Messages.MEMORY_COALESCING_BY_WHOLE_WARP);
		for (int i = 0; i < gkleeLog.memoryCoalescingByWholeWarpList.size(); i++) {
			MemoryCoalescingByWholeWarp memoryCoalescingByWholeWarp = gkleeLog.memoryCoalescingByWholeWarpList.get(i);
			subTreeItem = new TreeItem(treeItem, SWT.NONE);
			subTreeItem.setText(memoryCoalescingByWholeWarp.toString());
		}
	}

	private void updateBankConflicts(GkleeLog gkleeLog) {
		this.bankConflictTree.clearAll(true);
		Image image;
		// TODO change constant to preference
		if (gkleeLog.aveWarpBankConflictRate > 50) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.bankConflictTab.setImage(image);
		TreeItem treeItem, subTreeItem, subSubTreeItem;
		treeItem = new TreeItem(this.bankConflictTree, SWT.NONE);
		treeItem.setText(Messages.BANK_CONFLICTS_WITHIN_A_WARP);
		for (int i = 0; i < gkleeLog.threadBankConflicts.size(); i++) {
			ThreadBankConflict threadBankConflict = gkleeLog.threadBankConflicts.get(i);
			subTreeItem = new TreeItem(treeItem, SWT.NONE);
			subTreeItem.setText(threadBankConflict.getType().toString());
			subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
			subSubTreeItem.setText(threadBankConflict.getThread1().toString());
			// TODO make this double-clickable to jump to this line in the file
			subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
			subSubTreeItem.setText(threadBankConflict.getThread2().toString());
		}

		treeItem = new TreeItem(this.bankConflictTree, SWT.NONE);
		treeItem.setText(Messages.BANK_CONFLICTS_ACROSS_WARPS);
		for (int i = 0; i < gkleeLog.threadBankConflictsAcrossWarps.size(); i++) {
			ThreadBankConflict threadBankConflict = gkleeLog.threadBankConflictsAcrossWarps.get(i);
			subTreeItem = new TreeItem(treeItem, SWT.NONE);
			subTreeItem.setText(threadBankConflict.getType().toString());
			subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
			subSubTreeItem.setText(threadBankConflict.getThread1().toString());
			// TODO make this double-clickable to jump to this line in the file
			subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
			subSubTreeItem.setText(threadBankConflict.getThread2().toString());
		}
	}

	private void updateWarpDivergence(GkleeLog gkleeLog) {
		this.warpDivergenceTree.clearAll(true);
		Image image;
		// TODO change constant to preference
		if (gkleeLog.aveWarpDivergentRate > 50) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.warpDivergenceTab.setImage(image);
		TreeItem treeItem, subTreeItem, subSubTreeItem;
		for (int i = 0; i < gkleeLog.warpDivergences.size(); i++) {
			WarpDivergence warpDivergence = gkleeLog.warpDivergences.get(i);
			treeItem = new TreeItem(this.warpDivergenceTree, SWT.NONE);
			List<int[]> sets = warpDivergence.getSets();
			if (sets.size() > 1) {
				treeItem.setText(String.format(Messages.WARP_DIVERGES_INTO_FOLLOWING_SETS, warpDivergence.getWarpNumber()));
				for (int j = 0; j < sets.size(); j++) {
					subTreeItem = new TreeItem(treeItem, SWT.NONE);
					subTreeItem.setText(String.format(Messages.SET, j));
					StringBuilder stringBuilder = new StringBuilder();
					int[] set = sets.get(j);
					for (int k = 0; k < set.length; k++) {
						stringBuilder.append(set[k] + ", "); //$NON-NLS-1$
					}
					subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
					subSubTreeItem.setText(stringBuilder.toString());
				}
			}
			else {
				treeItem.setText(String.format(Messages.WARP_DOES_NOT_DIVERGE, warpDivergence.getWarpNumber()));
			}
		}
	}

	private void updateDeadlocks(GkleeLog gkleeLog) {
		this.deadlockTree.clearAll(true);
		Image image;
		if (gkleeLog.threadsThatWaitAtExplicitSyncThreads == null) {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
			TreeItem treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
			treeItem.setText(Messages.NO_DEADLOCK);
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/icon_error.gif").createImage(); //$NON-NLS-1$
			TreeItem treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
			treeItem.setText(Messages.THREADS_THAT_WAIT_AT_EXPLICIT_SYNC_THREADS);
			TreeItem subTreeItem;
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < gkleeLog.threadsThatWaitAtExplicitSyncThreads.length; i++) {
				stringBuilder.append(gkleeLog.threadsThatWaitAtExplicitSyncThreads[i] + ", "); //$NON-NLS-1$
			}
			subTreeItem = new TreeItem(treeItem, SWT.NONE);
			subTreeItem.setText(stringBuilder.toString());
			treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
			treeItem.setText(Messages.THREADS_THAT_WAIT_AT_THE_RECONVERGENT_POINT);
			stringBuilder = new StringBuilder();
			for (int i = 0; i < gkleeLog.threadsThatWaitAtReConvergentPoint.length; i++) {
				stringBuilder.append(gkleeLog.threadsThatWaitAtReConvergentPoint[i] + ", "); //$NON-NLS-1$
			}
			subTreeItem = new TreeItem(treeItem, SWT.NONE);
			subTreeItem.setText(stringBuilder.toString());
		}
		this.deadlockTab.setImage(image);
	}
}