package org.strangeforest.tcb.stats.model;

public class MatchStats {

	private final String winner;
	private final String loser;
	private final PlayerStats winnerStats;
	private final PlayerStats loserStats;

	public MatchStats(String winner, String loser, PlayerStats winnerStats, PlayerStats loserStats) {
		this.winner = winner;
		this.loser = loser;
		this.winnerStats = winnerStats;
		this.loserStats = loserStats;
		winnerStats.crossLinkOpponentStats(loserStats);
	}

	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}

	public PlayerStats getWinnerStats() {
		return winnerStats;
	}

	public PlayerStats getLoserStats() {
		return loserStats;
	}

	public boolean hasPointStats() {
		return winnerStats.hasPointStats() || loserStats.hasPointStats();
	}
}
