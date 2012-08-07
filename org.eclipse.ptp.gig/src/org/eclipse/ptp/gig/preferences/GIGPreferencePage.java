package org.eclipse.ptp.gig.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GIGPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String LOCAL = Messages.GIG_PREFERENCE_PAGE_1;
	public static final String USERNAME = Messages.USERNAME;
	public static final String PASSWORD = Messages.PASSWORD;
	public static final String GKLEE_HOME = "GKLEE_HOME"; //$NON-NLS-1$
	public static final String FLA_KLEE_HOME_DIR = "FLA_KLEE_HOME_DIR"; //$NON-NLS-1$
	public static final String GKLEE_DEBUG_PLUS_ASSERTS_BIN = "Gklee Debug+Asserts bin"; //$NON-NLS-1$
	public static final String LLVM_DEBUG_PLUS_ASSERTS_BIN = "llvm Debug+Asserts bin"; //$NON-NLS-1$
	public static final String LLVM_GCC_LINUX_BIN = "llvm gcc linux bin"; //$NON-NLS-1$
	public static final String BIN = "bin"; //$NON-NLS-1$
	public static final String ADDITIONAL_PATH = Messages.GIG_PREFERENCE_PAGE_0 + " PATH"; //$NON-NLS-1$

	public GIGPreferencePage() {
		super(GRID);
		setPreferenceStore(GIGPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite composite = this.getFieldEditorParent();
		Group localRemoteGroup = new Group(composite, SWT.NULL);
		Group remoteGroup = new Group(localRemoteGroup, SWT.NULL);
		Group localGroup = new Group(localRemoteGroup, SWT.NULL);
		this.addField(new BooleanFieldEditor(LOCAL, Messages.GIG_PREFERENCE_PAGE_1,
				BooleanFieldEditor.DEFAULT, localRemoteGroup));
		this.addField(new StringFieldEditor(GIGPreferencePage.USERNAME, GIGPreferencePage.USERNAME, remoteGroup));
		this.addField(new StringFieldEditor(GIGPreferencePage.PASSWORD, GIGPreferencePage.PASSWORD, remoteGroup));
		this.addField(new StringFieldEditor(GKLEE_HOME, GKLEE_HOME, localGroup));
		this.addField(new StringFieldEditor(FLA_KLEE_HOME_DIR, FLA_KLEE_HOME_DIR, localGroup));
		this.addField(new StringFieldEditor(GKLEE_DEBUG_PLUS_ASSERTS_BIN, GKLEE_DEBUG_PLUS_ASSERTS_BIN, localGroup));
		this.addField(new StringFieldEditor(LLVM_DEBUG_PLUS_ASSERTS_BIN, LLVM_DEBUG_PLUS_ASSERTS_BIN, localGroup));
		this.addField(new StringFieldEditor(LLVM_GCC_LINUX_BIN, LLVM_GCC_LINUX_BIN, localGroup));
		this.addField(new StringFieldEditor(BIN, BIN, localGroup));
		this.addField(new StringFieldEditor(ADDITIONAL_PATH, ADDITIONAL_PATH, localGroup));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
