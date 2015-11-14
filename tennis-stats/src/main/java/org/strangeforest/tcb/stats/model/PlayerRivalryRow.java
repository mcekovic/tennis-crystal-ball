package org.strangeforest.tcb.stats.model;

public class PlayerRivalryRow extends PlayerRow {

	private WonLost wonLost;
	private LastMatch lastMatch;

	public PlayerRivalryRow(int rank, int playerId, String name, String countryId) {
		super(rank, playerId, name, countryId);
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

	public LastMatch getLastMatch() {
		return lastMatch;
	}

	public void setLastMatch(LastMatch lastMatch) {
		this.lastMatch = lastMatch;
	}
}
