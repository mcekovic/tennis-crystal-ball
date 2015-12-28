package org.strangeforest.tcb.stats.model;

public class RankRangeGOATPointsRow {

	private final int fromRank;
	private final int toRank;
	private final int goatPoints;

	public RankRangeGOATPointsRow(int fromRank, int toRank, int goatPoints) {
		this.fromRank = fromRank;
		this.toRank = toRank;
		this.goatPoints = goatPoints;
	}

	public int getFromRank() {
		return fromRank;
	}

	public int getToRank() {
		return toRank;
	}

	public int getGoatPoints() {
		return goatPoints;
	}
}
