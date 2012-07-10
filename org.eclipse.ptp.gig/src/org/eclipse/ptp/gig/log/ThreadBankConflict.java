package org.eclipse.ptp.gig.log;

import org.eclipse.ptp.gig.messages.Messages;

public class ThreadBankConflict {

	public enum Type {
		RR,
		WW,
		RW;

		@Override
		public String toString() {
			switch (this) {
			case RR:
				return Messages.READ_READ_BANK_CONFLICT;
			case WW:
				return Messages.WRITE_WRITE_BANK_CONFLICT;
			case RW:
				return Messages.READ_WRITE_BANK_CONFLICT;
			}
			return null;
		}
	};

	private final ThreadBankConflictThreadInfo thread1, thread2;
	private final ThreadBankConflict.Type type;
	private int blockId;

	public ThreadBankConflict(ThreadBankConflictThreadInfo thread1, ThreadBankConflictThreadInfo thread2, String type, int blockId)
			throws LogException {
		this.thread1 = thread1;
		this.thread2 = thread2;
		if (type.equals("R-R")) { //$NON-NLS-1$
			this.type = Type.RR;
		}
		else if (type.equals("W-W")) { //$NON-NLS-1$
			this.type = Type.WW;
		}
		else if (type.equals("R-W")) { //$NON-NLS-1$
			this.type = Type.RW;
		}
		else {
			throw new LogException(Messages.LOG_EXCEPTION_THREAD_BANK_CONFLICT);
		}
		this.blockId = blockId;
	}

	public ThreadBankConflict(ThreadBankConflictThreadInfo thread1, ThreadBankConflictThreadInfo thread2, String type)
			throws LogException {
		this.thread1 = thread1;
		this.thread2 = thread2;
		if (type.equals("R-R")) { //$NON-NLS-1$
			this.type = Type.RR;
		}
		else if (type.equals("W-W")) { //$NON-NLS-1$
			this.type = Type.WW;
		}
		else if (type.equals("R-W")) { //$NON-NLS-1$
			this.type = Type.RW;
		}
		else {
			throw new LogException(Messages.LOG_EXCEPTION_THREAD_BANK_CONFLICT);
		}
	}

	public ThreadBankConflict.Type getType() {
		return type;
	}

	public ThreadBankConflictThreadInfo getThread1() {
		return thread1;
	}

	public ThreadBankConflictThreadInfo getThread2() {
		return thread2;
	}

}
