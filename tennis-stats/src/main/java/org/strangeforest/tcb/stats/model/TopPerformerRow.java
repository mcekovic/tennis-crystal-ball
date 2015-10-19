package org.strangeforest.tcb.stats.model;

public class TopPerformerRow extends PlayerRow {

	private final WonLost wonLost;

	public TopPerformerRow(int rank, int playerId, String player, String countryId, WonLost wonLost) {
		super(rank, playerId, player, countryId);
		this.wonLost = wonLost;
	}

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getLost() {
		return wonLost.getLost();
	}
}
