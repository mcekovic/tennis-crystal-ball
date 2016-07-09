package org.strangeforest.tcb.stats.model.records.rows;

public class SeasonWinningPctRecordDetail extends SeasonWonLostRecordDetail {

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}
}
