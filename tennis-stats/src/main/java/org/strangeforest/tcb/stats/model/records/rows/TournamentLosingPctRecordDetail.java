package org.strangeforest.tcb.stats.model.records.rows;

public class TournamentLosingPctRecordDetail extends TournamentWonLostRecordDetail {

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
