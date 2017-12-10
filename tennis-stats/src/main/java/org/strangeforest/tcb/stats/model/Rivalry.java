package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public class Rivalry {

	protected final RivalryPlayer player1, player2;
	protected WonLost wonLost;
	protected final MatchInfo lastMatch;

	public Rivalry(RivalryPlayer player1, RivalryPlayer player2, WonLost wonLost, MatchInfo lastMatch) {
		this.player1 = player1;
		this.player2 = player2;
		this.wonLost = wonLost;
		this.lastMatch = lastMatch;
	}

	public RivalryPlayer getPlayer1() {
		return player1;
	}

	public RivalryPlayer getPlayer2() {
		return player2;
	}

	void addWonLost(WonLost wonLost) {
		this.wonLost = this.wonLost.add(wonLost);
	}

	public int getMatches() {
		return wonLost.getTotal();
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getLost() {
		return wonLost.getLost();
	}

	public String getWonPctStr() {
		return wonLost.getWonPctStr();
	}

	public int getWonPctClass() {
		return wonLost.getWonPctClass();
	}

	public String getLostPctStr() {
		return wonLost.inverted().getWonPctStr();
	}

	public int getLostPctClass() {
		return wonLost.inverted().getWonPctClass();
	}

	public MatchInfo getLastMatch() {
		return lastMatch;
	}

	Rivalry inverted() {
		return new Rivalry(player2, player1, wonLost.inverted(), lastMatch);
	}
}
