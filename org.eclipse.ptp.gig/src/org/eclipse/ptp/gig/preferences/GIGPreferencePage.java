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

	public static final String LOCAL = Messages.GIG_PreferencePage_1;
	public static final String GKLEE_HOME = "GKLEE_HOME"; //$NON-NLS-1$
	public static final String FLA_KLEE_HOME_DIR = "FLA_KLEE_HOME_DIR"; //$NON-NLS-1$
	public static final String Gklee_DebugPlusAsserts_bin = "Gklee_DebugPlusAsserts_bin"; //$NON-NLS-1$
	public static final String llvm_DebugPlusAsserts_bin = "llvm_DebugPlusAsserts_bin"; //$NON-NLS-1$
	public static final String llvm_gcc_linux_bin = "llvm_gcc_linux_bin"; //$NON-NLS-1$
	public static final String bin = "bin"; //$NON-NLS-1$
	public static final String other_PATH = Messages.GIG_PreferencePage_0 + " PATH"; //$NON-NLS-1$

	public GIGPreferencePage() {
		super(GRID);
		setPreferenceStore(GIGPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite comp = this.getFieldEditorParent();
		Group group = new Group(comp, SWT.NULL);
		this.addField(new BooleanFieldEditor(LOCAL, Messages.GIG_PreferencePage_1,
				BooleanFieldEditor.DEFAULT, group));
		this.addField(new StringFieldEditor(GKLEE_HOME, GKLEE_HOME, group));
		this.addField(new StringFieldEditor(FLA_KLEE_HOME_DIR, FLA_KLEE_HOME_DIR, group));
		this.addField(new StringFieldEditor(Gklee_DebugPlusAsserts_bin, Gklee_DebugPlusAsserts_bin, group));
		this.addField(new StringFieldEditor(llvm_DebugPlusAsserts_bin, llvm_DebugPlusAsserts_bin, group));
		this.addField(new StringFieldEditor(llvm_gcc_linux_bin, llvm_gcc_linux_bin, group));
		this.addField(new StringFieldEditor(bin, bin, group));
		this.addField(new StringFieldEditor(other_PATH, other_PATH, group));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
