package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class LosingPctRecordDetail extends WonLostRecordDetail {

	public LosingPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost
	) {
		super(won, lost);
	}

	@Override public String getValue() {
		return wonLost.inverted().getWonPctStr(2);
	}
}
