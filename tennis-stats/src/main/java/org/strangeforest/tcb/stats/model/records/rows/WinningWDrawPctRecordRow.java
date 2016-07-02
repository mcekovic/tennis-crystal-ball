package org.strangeforest.tcb.stats.model.records.rows;

public class WinningWDrawPctRecordRow extends WonDrawLostRecordRow {

	public WinningWDrawPctRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getWonLostPct() {
		return wonDrawLost.getWonPctStr(2);
	}
}
