package org.strangeforest.tcb.stats.model;

public class MatchStats {

	private final String winner;
	private final String loser;
	private final PlayerStats winnerStats;
	private final PlayerStats loserStats;
	private final int minutes;

	public MatchStats(String winner, String loser, PlayerStats winnerStats, PlayerStats loserStats, int minutes) {
		this.winner = winner;
		this.loser = loser;
		this.minutes = minutes;
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

	public int getMinutes() {
		return minutes;
	}

	public String getTime() {
		int hours = minutes / 60;
		return String.format("%1$d:%2$02d", hours, minutes - hours * 60);
	}
}
