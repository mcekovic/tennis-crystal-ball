package org.strangeforest.tcb.stats.model;

public class MatchStats {

	private final String player1;
	private final String player2;
	private final PlayerStats stats1;
	private final PlayerStats stats2;

	public MatchStats(String player1, String player2, PlayerStats stats1, PlayerStats stats2) {
		this.player1 = player1;
		this.player2 = player2;
		this.stats1 = stats1;
		this.stats2 = stats2;
		stats1.crossLinkOpponentStats(stats2);
	}

	public String getPlayer1() {
		return player1;
	}

	public String getPlayer2() {
		return player2;
	}

	public PlayerStats getStats1() {
		return stats1;
	}

	public PlayerStats getStats2() {
		return stats2;
	}

	public boolean hasPointStats() {
		return stats1.hasPointStats() || stats2.hasPointStats();
	}
}
