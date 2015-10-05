package org.strangeforest.tcb.stats.model;

public class MatchStats {

	private final int minutes;
	private final Stats winnerStats;
	private final Stats loserStats;

	public MatchStats(int minutes, Stats winnerStats, Stats loserStats) {
		this.minutes = minutes;
		this.winnerStats = winnerStats;
		this.loserStats = loserStats;
		winnerStats.setOponentStats(loserStats);
		loserStats.setOponentStats(winnerStats);
	}

	public int getMinutes() {
		return minutes;
	}

	public Stats getWinnerStats() {
		return winnerStats;
	}

	public Stats getLoserStats() {
		return loserStats;
	}
}
