package org.strangeforest.tcb.stats.model;

public class RankGOATPoints {

	private final int rank;
	private final int goatPoints;

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

	public RankGOATPoints applyFactor(int factor) {
		return factor == 1 ? this : new RankGOATPoints(rank, goatPoints * factor);
	}

	public RankGOATPoints applyFactor(double factor) {
		return factor == 1.0 ? this : new RankGOATPoints(rank, (int)(goatPoints * factor));
	}
}
