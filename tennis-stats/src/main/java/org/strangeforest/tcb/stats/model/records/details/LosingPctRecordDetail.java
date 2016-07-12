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

	public String getLostWonPct() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toString() {
		return format("%1$s (%2$d/%3$d)", getLostWonPct(), wonLost.getLost(), wonLost.getTotal());
	}
}
