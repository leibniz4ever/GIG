package org.eclipse.ptp.gig.log;

import java.util.List;

public class WarpDivergence {
	private final List<int[]> sets;
	private final int warpNumber;

	public WarpDivergence(List<int[]> sets, int warp) {
		this.sets = sets;
		this.warpNumber = warp;
	}

	public List<int[]> getSets() {
		return sets;
	}

	public int getWarpNumber() {
		return warpNumber;
	}

}
