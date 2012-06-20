package org.eclipse.ptp.gig.messages;

import org.eclipse.osgi.util.NLS;

/*
 * Used for strings that should be translated into different languages
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.gig.messages.messages"; //$NON-NLS-1$

	public static String GIG_PREFERENCE_PAGE_0;
	public static String GIG_PREFERENCE_PAGE_1;
	public static String IO_EXCEPTION;
	public static String INTERRUPTED_EXCEPTION;
	public static String RUN_GKLEE;
	public static String PART_INIT_EXCEPTION;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
