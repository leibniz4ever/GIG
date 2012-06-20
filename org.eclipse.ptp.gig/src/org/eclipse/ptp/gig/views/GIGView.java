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

import org.eclipse.ptp.gig.GkleeLog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class GIGView extends ViewPart {

	public static final String ID = "org.eclipse.ptp.gig.views.GIGView"; //$NON-NLS-1$
	private static GIGView view;

	public GIGView() {
		super();
		view = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static GIGView getDefault() {
		return view;
	}

	/*
	 * Takes a GkleeLog and updates this views components based on it
	 */
	public void update(GkleeLog gkleeLog) {
		// TODO Auto-generated method stub

	}

}