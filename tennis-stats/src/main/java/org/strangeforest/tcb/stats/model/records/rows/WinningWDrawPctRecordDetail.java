package org.strangeforest.tcb.stats.model.records.rows;

public class WinningWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public String getWonLostPct() {
		return wonDrawLost.getWonPctStr(2);
	}
}
