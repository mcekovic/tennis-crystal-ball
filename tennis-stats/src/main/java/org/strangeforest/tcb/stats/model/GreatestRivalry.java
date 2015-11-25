package org.strangeforest.tcb.stats.model;

public class GreatestRivalry extends Rivalry {

	private final int rank;

	public GreatestRivalry(int rank, RivalryPlayer player1, RivalryPlayer player2, WonLost wonLost, LastMatch lastMatch) {
		super(player1, player2, wonLost, lastMatch);
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}
}
