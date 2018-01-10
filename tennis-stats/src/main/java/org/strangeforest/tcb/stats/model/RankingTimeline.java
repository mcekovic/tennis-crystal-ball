package org.strangeforest.tcb.stats.model;

import java.util.*;

public class RankingTimeline {

	private final WeeksAtRank careerWeeksAtRank = new WeeksAtRank(false);
	private final Map<Integer, WeeksAtRank> seasonsWeeksAtRank = new HashMap<>();

	public WeeksAtRank getCareerWeeksAtRank() {
		return careerWeeksAtRank;
	}

	public Map<Integer, WeeksAtRank> getSeasonsWeeksAtRank() {
		return seasonsWeeksAtRank;
	}

	public void processWeeksAt(int season, int rank, double weeks, double seasonWeeks, double nextSeasonWeeks) {
		if (rank > 0) {
			careerWeeksAtRank.processWeeksAt(rank, weeks);
			seasonsWeeksAtRank.computeIfAbsent(season, s -> new WeeksAtRank(true)).processWeeksAt(rank, seasonWeeks);
			seasonsWeeksAtRank.computeIfAbsent(season + 1, s -> new WeeksAtRank(true)).processWeeksAt(rank, nextSeasonWeeks);
		}
	}
}
