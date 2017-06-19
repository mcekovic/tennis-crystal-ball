package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class PeakLosingPctRecordDetail extends PeakWonLostRecordDetail {

	public PeakLosingPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("date") Date date
	) {
		super(won, lost, date);
	}

	@Override public String getValue() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%3$td-%3$tm-%3$tY %1$d/%2$d", wonLost.getLost(), wonLost.getTotal(), getDate());
	}
}
