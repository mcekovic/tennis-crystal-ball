package org.strangeforest.tcb.stats.model.records.rows;

public class LosingWDrawPctRecordRow extends WonDrawLostRecordRow {

	public LosingWDrawPctRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getLostWonPct() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}
}
