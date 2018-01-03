package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class WinningPctRecordDetail extends WonLostRecordDetail {

	public WinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost
	) {
		super(won, lost);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}
}
