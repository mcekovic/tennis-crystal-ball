package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public class PlayerRivalryRow extends PlayerRow {

	private WonLost wonLost;
	private MatchInfo lastMatch;

	public PlayerRivalryRow(int rank, int playerId, String name, String countryId, boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getBestRank() {
		return getRank();
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

	public void setWonLost(WonLost wonLost) {
		this.wonLost = wonLost;
	}

	public MatchInfo getLastMatch() {
		return lastMatch;
	}

	public void setLastMatch(MatchInfo lastMatch) {
		this.lastMatch = lastMatch;
	}
}
