package org.eclipse.ptp.gig.preferences;

import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.GIGPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore pstore = GIGPlugin.getDefault().getPreferenceStore();

		// find the HOME directory as a start
		ProcessBuilder pb = new ProcessBuilder();
		Map<String, String> env = pb.environment();
		String home = env.get("HOME"); //$NON-NLS-1$
		pb = null;
		env = null;

		// assume default installation directories
		String gkleeHome = home + "/gklee"; //$NON-NLS-1$
		String flaKleeHomeDir = gkleeHome;
		String gkleeDebugPlusAssertsBin = gkleeHome + "/Gklee/Debug+Asserts/bin"; //$NON-NLS-1$
		String llvmDebugPlusAssertsBin = gkleeHome + "/llvm-2.8/Debug+Asserts/bin"; //$NON-NLS-1$
		String llvmGccLinuxBin = gkleeHome + "/llvm-gcc4.2-2.8-x86_64-linux/bin"; //$NON-NLS-1$
		String bin = gkleeHome + "/bin"; //$NON-NLS-1$
		String otherPATH = ""; //$NON-NLS-1$

		pstore.setDefault(GIGPreferencePage.LOCAL, false);
		pstore.setDefault(GIGPreferencePage.USERNAME, "gklee"); //$NON-NLS-1$
		pstore.setDefault(GIGPreferencePage.PASSWORD, ""); //$NON-NLS-1$
		pstore.setDefault(GIGPreferencePage.GKLEE_HOME, gkleeHome);
		pstore.setDefault(GIGPreferencePage.FLA_KLEE_HOME_DIR, flaKleeHomeDir);
		pstore.setDefault(GIGPreferencePage.GKLEE_DEBUG_PLUS_ASSERTS_BIN, gkleeDebugPlusAssertsBin);
		pstore.setDefault(GIGPreferencePage.LLVM_DEBUG_PLUS_ASSERTS_BIN, llvmDebugPlusAssertsBin);
		pstore.setDefault(GIGPreferencePage.LLVM_GCC_LINUX_BIN, llvmGccLinuxBin);
		pstore.setDefault(GIGPreferencePage.BIN, bin);
		pstore.setDefault(GIGPreferencePage.ADDITIONAL_PATH, otherPATH);
		/*
		 * String HOME = "HOME"; //$NON-NLS-1$
		 * String HOME_value = env.get(HOME);
		 * String GKLEE_HOME = "GKLEE_HOME"; //$NON-NLS-1$
		 * String GKLEE_HOME_value = HOME_value + "/gklee"; //$NON-NLS-1$
		 * env.put(GKLEE_HOME, GKLEE_HOME_value);
		 * String FLA_KLEE_HOME_DIR = "FLA_KLEE_HOME_DIR"; //$NON-NLS-1$
		 * String FLA_KLEE_HOME_DIR_value = GKLEE_HOME_value;
		 * env.put(FLA_KLEE_HOME_DIR, FLA_KLEE_HOME_DIR_value);
		 * String PATH = "PATH"; //$NON-NLS-1$
		 * String PATH_value = env.get(PATH) + ':' + GKLEE_HOME_value + "/Gklee/Debug+Asserts/bin:" + GKLEE_HOME_value //$NON-NLS-1$
		 * + "/llvm-2.8/Debug+Asserts/bin:" + GKLEE_HOME_value + "/llvm-gcc4.2-2.8-x86_64-linux/bin:" + GKLEE_HOME_value
		 * //$NON-NLS-1$ //$NON-NLS-2$
		 * + "/bin"; //$NON-NLS-1$
		 * env.put(PATH, PATH_value);
		 */
	}
}
