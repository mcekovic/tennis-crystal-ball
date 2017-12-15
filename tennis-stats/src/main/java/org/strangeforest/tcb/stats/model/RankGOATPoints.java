package org.strangeforest.tcb.stats.model;

public class RankGOATPoints {

	private final int rank;
	private int goatPoints;

	public RankGOATPoints(int rank, int goatPoints) {
		this.rank = rank;
		this.goatPoints = goatPoints;
	}

	public int getRank() {
		return rank;
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public void applyFactor(int factor) {
		goatPoints *= factor;
	}
}
