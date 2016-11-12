package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class SurfaceTimelineItem {

	private final int season;
	private final int matchCount;
	private final int hardMatchCount;
	private final int clayMatchCount;
	private final int grassMatchCount;
	private final int carpetMatchCount;

	public SurfaceTimelineItem(int season, int matchCount, int hardMatchCount, int clayMatchCount, int grassMatchCount, int carpetMatchCount) {
		this.season = season;
		this.matchCount = matchCount;
		this.hardMatchCount = hardMatchCount;
		this.clayMatchCount = clayMatchCount;
		this.grassMatchCount = grassMatchCount;
		this.carpetMatchCount = carpetMatchCount;
	}

	public int getSeason() {
		return season;
	}

	public double getHardPct() {
		return pct(hardMatchCount, matchCount);
	}

	public double getClayPct() {
		return pct(clayMatchCount, matchCount);
	}

	public double getGrassPct() {
		return pct(grassMatchCount, matchCount);
	}

	public double getCarpetPct() {
		return pct(carpetMatchCount, matchCount);
	}

	public double getUnknownPct() {
		return pct(matchCount - hardMatchCount - clayMatchCount - grassMatchCount - carpetMatchCount, matchCount);
	}
}
