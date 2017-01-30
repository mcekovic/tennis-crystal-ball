package org.strangeforest.tcb.stats.model;

public class GreatestRivalry extends Rivalry {

	private final int rank;
	private final int rivalryScore;

	public GreatestRivalry(int rank, RivalryPlayer player1, RivalryPlayer player2, WonLost wonLost, int rivalryScore, MatchInfo lastMatch) {
		super(player1, player2, wonLost, lastMatch);
		this.rank = rank;
		this.rivalryScore = rivalryScore;
	}

	public int getRank() {
		return rank;
	}

	public int getRivalryScore() {
		return rivalryScore;
	}
}
