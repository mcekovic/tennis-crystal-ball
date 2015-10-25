package org.strangeforest.tcb.stats.model;

public class YearEndRankGOATPointsRow {

	private final int yearEndRank;
	private final int goatPoints;

	public YearEndRankGOATPointsRow(int yearEndRank, int goatPoints) {
		this.yearEndRank = yearEndRank;
		this.goatPoints = goatPoints;
	}

	public int getYearEndRank() {
		return yearEndRank;
	}

	public int getGoatPoints() {
		return goatPoints;
	}
}
