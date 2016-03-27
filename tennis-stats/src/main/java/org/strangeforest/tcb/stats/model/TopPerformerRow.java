package org.strangeforest.tcb.stats.model;

public class TopPerformerRow extends PlayerRow {

	private final WonLost wonLost;

	public TopPerformerRow(int rank, int playerId, String name, String countryId, boolean active, WonLost wonLost) {
		super(rank, playerId, name, countryId, active);
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
