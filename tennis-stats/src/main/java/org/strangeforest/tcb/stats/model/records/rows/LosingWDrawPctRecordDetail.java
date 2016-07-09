package org.strangeforest.tcb.stats.model.records.rows;

public class LosingWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public String getLostWonPct() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}
}
