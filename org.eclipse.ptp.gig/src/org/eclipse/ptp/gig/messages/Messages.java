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
	public static String PARSE_EXCEPTION;
	public static String LOG_EXCEPTION_THREAD_BANK_CONFLICT;
	public static String MEMORY_COALESCING;
	public static String BANK_CONFLICTS;
	public static String WARP_DIVERGENCE;
	public static String DEADLOCKS;
	public static String NO_DEADLOCK;
	public static String THREADS_THAT_WAIT_AT_EXPLICIT_SYNC_THREADS;
	public static String THREADS_THAT_WAIT_AT_THE_RECONVERGENT_POINT;
	public static String UPDATE_GIG;
	public static String WARP_DIVERGES_INTO_FOLLOWING_SETS;
	public static String WARP_DOES_NOT_DIVERGE;
	public static String SET;
	public static String READ_READ_BANK_CONFLICT;
	public static String WRITE_WRITE_BANK_CONFLICT;
	public static String READ_WRITE_BANK_CONFLICT;
	public static String BANK_CONFLICTS_WITHIN_A_WARP;
	public static String BANK_CONFLICTS_ACROSS_WARPS;
	public static String THREAD_BANK_CONFLICT_THREAD_INFO_TO_STRING;
	public static String MEMORY_COALESCING_BY_WHOLE_WARP;
	public static String MEMORY_COALESCING_BY_WHOLE_WARP_TO_STRING;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
