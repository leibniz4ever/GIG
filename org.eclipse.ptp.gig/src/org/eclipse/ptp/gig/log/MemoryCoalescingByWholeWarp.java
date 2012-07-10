package org.eclipse.ptp.gig.log;

import org.eclipse.ptp.gig.messages.Messages;

public class MemoryCoalescingByWholeWarp {
	private final int warp, numThreads, request, totalRequests, memorySegment;

	public MemoryCoalescingByWholeWarp(int warp, int numThreads, int request, int totalRequests, int memorySegment) {
		this.warp = warp;
		this.numThreads = numThreads;
		this.request = request;
		this.totalRequests = totalRequests;
		this.memorySegment = memorySegment;
	}

	@Override
	public String toString() {
		return String.format(Messages.MEMORY_COALESCING_BY_WHOLE_WARP_TO_STRING, warp, numThreads);
	}

}
