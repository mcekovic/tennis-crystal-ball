package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.format;

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

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%1$d/%2$d", wonLost.getLost(), wonLost.getTotal());
	}
}
