package org.eclipse.ptp.gig.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GIGPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GIGPreferencePage() {
		super(GRID);
		setPreferenceStore(GIGPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
