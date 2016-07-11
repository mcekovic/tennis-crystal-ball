package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class LosingWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public LosingWDrawPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("draw") int draw,
		@JsonProperty("lost") int lost
	) {
		super(won, draw, lost);
	}

	public String getLostWonPct() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}
}
