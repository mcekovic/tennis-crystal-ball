package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class WinningWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public WinningWDrawPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("draw") int draw,
		@JsonProperty("lost") int lost
	) {
		super(won, draw, lost);
	}

	public String getWonLostPct() {
		return wonDrawLost.getWonPctStr(2);
	}
}
