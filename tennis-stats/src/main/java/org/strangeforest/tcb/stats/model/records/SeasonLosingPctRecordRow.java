package org.strangeforest.tcb.stats.model.records;

public class SeasonLosingPctRecordRow extends SeasonWonLostRecordRow {

	public SeasonLosingPctRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getLostWonPct() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}
}
