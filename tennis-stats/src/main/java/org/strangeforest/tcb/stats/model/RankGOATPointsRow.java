package org.strangeforest.tcb.stats.model;

public class RankGOATPointsRow {

	private final int rank;
	private final int goatPoints;

	public RankGOATPointsRow(int rank, int goatPoints) {
		this.rank = rank;
		this.goatPoints = goatPoints;
	}

	public int getRank() {
		return rank;
	}

	public int getGoatPoints() {
		return goatPoints;
	}
}
