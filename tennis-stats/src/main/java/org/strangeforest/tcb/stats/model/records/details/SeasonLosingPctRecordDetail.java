package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class SeasonLosingPctRecordDetail extends SeasonWonLostRecordDetail {

	public SeasonLosingPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("season") int season
	) {
		super(won, lost, season);
	}

	@Override public String getValue() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%3$d %1$d/%2$d", wonLost.getLost(), wonLost.getTotal(), getSeason());
	}
}
