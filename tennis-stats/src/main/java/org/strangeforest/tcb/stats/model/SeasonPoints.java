package org.strangeforest.tcb.stats.model;

public class SeasonPoints {

	private final int season;
	private final int points;

	public SeasonPoints(int season, int points) {
		this.season = season;
		this.points = points;
	}

	public int getSeason() {
		return season;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsRounded() {
		return 10 * (points / 10);
	}
}
