package org.strangeforest.tcb.stats.model;

public class MatchStats {

	private final String winner;
	private final String loser;
	private final Stats winnerStats;
	private final Stats loserStats;
	private final int minutes;

	public MatchStats(String winner, String loser, Stats winnerStats, Stats loserStats, int minutes) {
		this.winner = winner;
		this.loser = loser;
		this.minutes = minutes;
		this.winnerStats = winnerStats;
		this.loserStats = loserStats;
		winnerStats.setOpponentStats(loserStats);
		loserStats.setOpponentStats(winnerStats);
	}

	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}

	public Stats getWinnerStats() {
		return winnerStats;
	}

	public Stats getLoserStats() {
		return loserStats;
	}

	public int getMinutes() {
		return minutes;
	}
}
