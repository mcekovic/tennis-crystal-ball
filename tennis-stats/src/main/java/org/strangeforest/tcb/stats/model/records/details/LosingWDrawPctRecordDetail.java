package org.strangeforest.tcb.stats.model.records.details;

public class LosingWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public String getLostWonPct() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}
}
