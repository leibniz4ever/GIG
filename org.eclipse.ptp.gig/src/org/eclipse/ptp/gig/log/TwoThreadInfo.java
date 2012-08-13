package org.eclipse.ptp.gig.log;

import org.eclipse.core.resources.IFile;

public class TwoThreadInfo {
	private final ThreadInfo threadInfo0, threadInfo1;
	private final IFile logFile;
	private final int line;

	public TwoThreadInfo(ThreadInfo threadInfo0, ThreadInfo threadInfo1, IFile logFile, int line) {
		this.threadInfo0 = threadInfo0;
		this.threadInfo1 = threadInfo1;
		this.logFile = logFile;
		this.line = line;
	}

	public ThreadInfo getThreadInfo0() {
		return threadInfo0;
	}

	public ThreadInfo getThreadInfo1() {
		return threadInfo1;
	}

	public IFile getFile() {
		return this.logFile;
	}

	public int getLine() {
		return this.line;
	}

}
