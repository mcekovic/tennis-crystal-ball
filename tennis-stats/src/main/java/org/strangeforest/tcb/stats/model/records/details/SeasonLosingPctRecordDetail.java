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

	public String getLostWonPct() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toString() {
		return format("%1$s (%2$d/%3$d in %4$d)", getLostWonPct(), wonLost.getLost(), wonLost.getTotal(), getSeason());
	}
}
