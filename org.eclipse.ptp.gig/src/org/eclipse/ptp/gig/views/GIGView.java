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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.log.GkleeLog;
import org.eclipse.ptp.gig.log.OrganizedThreadInfo;
import org.eclipse.ptp.gig.log.WarpDivergence;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.util.GIGUtilities;
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
	private Tree memoryCoalescingTree, bankConflictTree, warpDivergenceTree, deadlockTree, raceTree, otherTree;
	private CTabItem memoryCoalescingTab, bankConflictTab, warpDivergenceTab, deadlockTab, raceTab, otherTab;

	private CTabFolder cTabFolder;
	private IAction cancelAction;

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

		cancelAction = new Action() {
			@Override
			public void run() {
				GIGUtilities.doCancel();
			}
		};
		cancelAction.setText(Messages.CANCEL);
		cancelAction.setToolTipText(Messages.CANCEL);
		cancelAction.setImageDescriptor(GIGPlugin.getImageDescriptor("icons/terminatedlaunch_obj.gif")); //$NON-NLS-1$
		this.getViewSite().getActionBars().getToolBarManager().add(cancelAction);

		cTabFolder = new CTabFolder(parent, SWT.TOP);

		reset();
	}

	private void reset() {
		CTabItem[] cTabItems = cTabFolder.getItems();
		for (int i = 0; i < cTabItems.length; i++) {
			cTabItems[i].dispose();
		}

		CTabItem cTabItem;

		int i = 0;
		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.RACES);
		this.raceTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.raceTree);
		this.raceTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.DEADLOCKS);
		this.deadlockTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.deadlockTree);
		this.deadlockTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.BANK_CONFLICTS);
		this.bankConflictTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.bankConflictTree);
		this.bankConflictTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.WARP_DIVERGENCE);
		this.warpDivergenceTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.warpDivergenceTree);
		this.warpDivergenceTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.MEMORY_COALESCING);
		this.memoryCoalescingTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.memoryCoalescingTree);
		this.memoryCoalescingTab = cTabItem;

		cTabItem = new CTabItem(cTabFolder, SWT.NONE, i++);
		cTabItem.setText(Messages.OTHER);
		this.otherTree = new Tree(cTabFolder, SWT.NONE);
		cTabItem.setControl(this.otherTree);
		this.otherTab = cTabItem;
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
	public void update(GkleeLog gkleeLog, IProject project) {
		reset();
		updateMemoryCoalescing(gkleeLog, project);
		updateBankConflicts(gkleeLog, project);
		updateWarpDivergence(gkleeLog, project);
		updateDeadlocks(gkleeLog, project);
		updateOther(gkleeLog, project);
		updateRaces(gkleeLog, project);
	}

	private void updateRaces(GkleeLog gkleeLog, IProject project) {
		this.raceTree.clearAll(true);
		Image image;
		OrganizedThreadInfo wwrwb, wwrw, wwrawb, wwraw, rwraw, wwbdb, wwbd, rwbd, rw, ww;
		wwrwb = gkleeLog.getWwrwb();
		wwrw = gkleeLog.getWwrw();
		wwrawb = gkleeLog.getWwrawb();
		wwraw = gkleeLog.getWwraw();
		rwraw = gkleeLog.getRwraw();
		wwbdb = gkleeLog.getWwbdb();
		wwbd = gkleeLog.getWwbd();
		rwbd = gkleeLog.getRwbd();
		rw = gkleeLog.getRw();
		ww = gkleeLog.getWw();
		boolean races = !(wwrwb.isEmpty() && wwrw.isEmpty() && wwrawb.isEmpty() && wwraw.isEmpty() && rwraw.isEmpty()
				&& wwbdb.isEmpty() && wwbd.isEmpty() && rwbd.isEmpty() && rw.isEmpty() && ww.isEmpty());
		if (races) {
			image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
			wwrwb.setupTree(this.raceTree, Messages.WWRWB, null, Messages.WWRWB, project);
			wwrw.setupTree(this.raceTree, Messages.WWRW, null, Messages.WWRW, project);
			wwrawb.setupTree(this.raceTree, Messages.WWRAWB, null, Messages.WWRAWB, project);
			wwraw.setupTree(this.raceTree, Messages.WWRAW, null, Messages.WWRAW, project);
			rwraw.setupTree(this.raceTree, Messages.RWRAW, null, Messages.RWRAW, project);
			wwbdb.setupTree(this.raceTree, Messages.WWBDB, null, Messages.WWBDB, project);
			wwbd.setupTree(this.raceTree, Messages.WWBD, null, Messages.WWBD, project);
			rwbd.setupTree(this.raceTree, Messages.RWBD, null, Messages.RWBD, project);
			rw.setupTree(this.raceTree, Messages.RW, null, Messages.RW, project);
			ww.setupTree(this.raceTree, Messages.WW, null, Messages.WW, project);
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
			TreeItem item = new TreeItem(raceTree, SWT.NONE);
			item.setText(Messages.NO + Messages.RACES);
		}
		this.raceTab.setImage(image);
	}

	private void updateOther(GkleeLog gkleeLog, IProject project) {
		this.otherTree.clearAll(true);
		Image image;
		OrganizedThreadInfo missingVolatile, assertion;
		missingVolatile = gkleeLog.getMissingVolatile();
		assertion = gkleeLog.getAssertions();
		boolean other = !(missingVolatile.isEmpty() && assertion.isEmpty());
		if (other) {
			image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.otherTab.setImage(image);

		assertion.setupTree(this.otherTree, Messages.ASSERTION_VIOLATION, Messages.NO + Messages.ASSERTION_VIOLATION,
				Messages.ASSERTION_VIOLATION, project);
		missingVolatile.setupTree(this.otherTree, Messages.MISSING_VOLATILE, Messages.NO + Messages.MISSING_VOLATILE,
				Messages.MISSING_VOLATILE, project);
	}

	private void updateMemoryCoalescing(GkleeLog gkleeLog, IProject project) {
		this.memoryCoalescingTree.clearAll(true);
		Image image;
		// TODO change constant to preference
		int rate = gkleeLog.getMemoryCoalescingRate();
		if (rate < 10) {
			image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
		}
		else if (rate < 50) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.memoryCoalescingTab.setImage(image);
		gkleeLog.getMemoryCoalescingStats().setupTree(this.memoryCoalescingTree, gkleeLog.getMemoryCoalescingLocation());
		gkleeLog.getMemoryCoalescing().setupTree(this.memoryCoalescingTree, Messages.NONCOALESCED_GLOBAL_MEMORY_ACCESSES,
				Messages.NO + Messages.NONCOALESCED_GLOBAL_MEMORY_ACCESSES, Messages.NONCOALESCED_GLOBAL_MEMORY_ACCESS, project);

		// TreeItem treeItem, subTreeItem;
		// treeItem = new TreeItem(this.memoryCoalescingTree, SWT.NONE);
		// treeItem.setText(Messages.MEMORY_COALESCING_BY_WHOLE_WARP);
		// for (int i = 0; i < gkleeLog.memoryCoalescingByWholeWarpList.size(); i++) {
		// MemoryCoalescingByWholeWarp memoryCoalescingByWholeWarp = gkleeLog.memoryCoalescingByWholeWarpList.get(i);
		// subTreeItem = new TreeItem(treeItem, SWT.NONE);
		// subTreeItem.setText(memoryCoalescingByWholeWarp.toString());
		// }
	}

	private void updateBankConflicts(GkleeLog gkleeLog, IProject project) {
		this.bankConflictTree.clearAll(true);
		Image image;
		int rate = gkleeLog.getBankConflictRate();
		// TODO change constant to preference
		if (rate > 50) {
			image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
		}
		else if (rate > 0) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.bankConflictTab.setImage(image);
		gkleeLog.getBankConflictStats().setupTree(this.bankConflictTree, null);
		gkleeLog.getRrbc().setupTree(this.bankConflictTree, Messages.READ_READ_BANK_CONFLICT,
				Messages.NO + Messages.READ_READ_BANK_CONFLICT, Messages.READ_READ_BANK_CONFLICT, project);
		gkleeLog.getRwbc().setupTree(this.bankConflictTree, Messages.READ_WRITE_BANK_CONFLICT,
				Messages.NO + Messages.READ_WRITE_BANK_CONFLICT, Messages.READ_WRITE_BANK_CONFLICT, project);
		gkleeLog.getWwbc().setupTree(this.bankConflictTree, Messages.WRITE_WRITE_BANK_CONFLICT,
				Messages.NO + Messages.WRITE_WRITE_BANK_CONFLICT, Messages.WRITE_WRITE_BANK_CONFLICT, project);
		// TreeItem treeItem, subTreeItem, subSubTreeItem;
		// treeItem = new TreeItem(this.bankConflictTree, SWT.NONE);
		// treeItem.setText(Messages.BANK_CONFLICTS_WITHIN_A_WARP);
		// for (int i = 0; i < gkleeLog.threadBankConflicts.size(); i++) {
		// ThreadBankConflict threadBankConflict = gkleeLog.threadBankConflicts.get(i);
		// subTreeItem = new TreeItem(treeItem, SWT.NONE);
		// subTreeItem.setText(threadBankConflict.getType().toString());
		// subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
		// subSubTreeItem.setText(threadBankConflict.getThread1().toString());
		// // TODO make this double-clickable to jump to this line in the file
		// subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
		// subSubTreeItem.setText(threadBankConflict.getThread2().toString());
		// }
		//
		// treeItem = new TreeItem(this.bankConflictTree, SWT.NONE);
		// treeItem.setText(Messages.BANK_CONFLICTS_ACROSS_WARPS);
		// for (int i = 0; i < gkleeLog.threadBankConflictsAcrossWarps.size(); i++) {
		// ThreadBankConflict threadBankConflict = gkleeLog.threadBankConflictsAcrossWarps.get(i);
		// subTreeItem = new TreeItem(treeItem, SWT.NONE);
		// subTreeItem.setText(threadBankConflict.getType().toString());
		// subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
		// subSubTreeItem.setText(threadBankConflict.getThread1().toString());
		// // TODO make this double-clickable to jump to this line in the file
		// subSubTreeItem = new TreeItem(subTreeItem, SWT.NONE);
		// subSubTreeItem.setText(threadBankConflict.getThread2().toString());
		// }
	}

	private void updateWarpDivergence(GkleeLog gkleeLog, IProject project) {
		this.warpDivergenceTree.clearAll(true);
		Image image;
		int rate = gkleeLog.getWarpDivergenceRate();
		// TODO change constant to preference
		if (rate > 50) {
			image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
		}
		else if (rate > 0) {
			image = GIGPlugin.getImageDescriptor("icons/icon_warning.gif").createImage(); //$NON-NLS-1$
		}
		else {
			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		}
		this.warpDivergenceTab.setImage(image);
		gkleeLog.getWarpDivergenceStats().setupTree(this.warpDivergenceTree, null);
		TreeItem treeItem, subTreeItem, subSubTreeItem;
		List<WarpDivergence> warpDivergences = gkleeLog.getWarpDivergences();
		int size = warpDivergences.size();
		for (int i = 0; i < size; i++) {
			WarpDivergence warpDivergence = warpDivergences.get(i);
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

	private void updateDeadlocks(GkleeLog gkleeLog, IProject project) {
		this.deadlockTree.clearAll(true);
		Image image;
		boolean potential, deadlock;
		OrganizedThreadInfo deadlocks, potentialSame, potentialVaried;
		deadlocks = gkleeLog.getDeadlocks();
		potentialSame = gkleeLog.getPotentialSame();
		potentialVaried = gkleeLog.getPotentialVaried();
		deadlock = !deadlocks.isEmpty();
		if (deadlock) {
			image = GIGPlugin.getImageDescriptor("icons/icon_error.gif").createImage(); //$NON-NLS-1$
		}
		else {
			potential = !(potentialSame.isEmpty() && potentialVaried.isEmpty());
			if (potential) {
				image = GIGPlugin.getImageDescriptor("icons/errorwarning_tab.gif").createImage(); //$NON-NLS-1$
			}
			else {
				image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
			}
		}
		this.deadlockTab.setImage(image);
		deadlocks.setupTree(this.deadlockTree, Messages.DEADLOCKS, Messages.NO_DEADLOCK, Messages.DEADLOCKS, project);
		potentialSame.setupTree(this.deadlockTree, Messages.POTENTIAL_DEADLOCK_SAME_LENGTH, Messages.NO
				+ Messages.POTENTIAL_DEADLOCK_SAME_LENGTH, Messages.POTENTIAL_DEADLOCK_SAME_LENGTH, project);
		potentialVaried.setupTree(this.deadlockTree, Messages.POTENTIAL_DEADLOCK_VARIED_LENGTH, Messages.NO
				+ Messages.POTENTIAL_DEADLOCK_VARIED_LENGTH, Messages.POTENTIAL_DEADLOCK_VARIED_LENGTH, project);
		// if (gkleeLog.threadsThatWaitAtExplicitSyncThreads == null) {
		//			image = GIGPlugin.getImageDescriptor("icons/no-error.gif").createImage(); //$NON-NLS-1$
		// TreeItem treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
		// treeItem.setText(Messages.NO_DEADLOCK);
		// }
		// else {
		//			image = GIGPlugin.getImageDescriptor("icons/icon_error.gif").createImage(); //$NON-NLS-1$
		// TreeItem treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
		// treeItem.setText(Messages.THREADS_THAT_WAIT_AT_EXPLICIT_SYNC_THREADS);
		// TreeItem subTreeItem;
		// StringBuilder stringBuilder = new StringBuilder();
		// for (int i = 0; i < gkleeLog.threadsThatWaitAtExplicitSyncThreads.length; i++) {
		//				stringBuilder.append(gkleeLog.threadsThatWaitAtExplicitSyncThreads[i] + ", "); //$NON-NLS-1$
		// }
		// subTreeItem = new TreeItem(treeItem, SWT.NONE);
		// subTreeItem.setText(stringBuilder.toString());
		// treeItem = new TreeItem(this.deadlockTree, SWT.NONE);
		// treeItem.setText(Messages.THREADS_THAT_WAIT_AT_THE_RECONVERGENT_POINT);
		// stringBuilder = new StringBuilder();
		// for (int i = 0; i < gkleeLog.threadsThatWaitAtReConvergentPoint.length; i++) {
		//				stringBuilder.append(gkleeLog.threadsThatWaitAtReConvergentPoint[i] + ", "); //$NON-NLS-1$
		// }
		// subTreeItem = new TreeItem(treeItem, SWT.NONE);
		// subTreeItem.setText(stringBuilder.toString());
		// }
	}
}