package org.strangeforest.tcb.stats.model;

public class RankRangeGOATPointsRow {

	private final int rankFrom;
	private final int rankTo;
	private final int goatPoints;

	public RankRangeGOATPointsRow(int rankFrom, int rankTo, int goatPoints) {
		this.rankFrom = rankFrom;
		this.rankTo = rankTo;
		this.goatPoints = goatPoints;
	}

	public int getRankFrom() {
		return rankFrom;
	}

	public int getRankTo() {
		return rankTo;
	}

	public int getGoatPoints() {
		return goatPoints;
	}
}
