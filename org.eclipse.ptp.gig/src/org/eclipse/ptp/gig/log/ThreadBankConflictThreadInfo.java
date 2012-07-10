package org.eclipse.ptp.gig.log;

import org.eclipse.ptp.gig.messages.Messages;

public class ThreadBankConflictThreadInfo {
	private final String filename;
	private final int tid, lineNumber;

	public ThreadBankConflictThreadInfo(String filename, int tid, int lineNumber) {
		this.filename = filename;
		this.tid = tid;
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return String.format(Messages.THREAD_BANK_CONFLICT_THREAD_INFO_TO_STRING, tid, lineNumber, filename);
	}
}