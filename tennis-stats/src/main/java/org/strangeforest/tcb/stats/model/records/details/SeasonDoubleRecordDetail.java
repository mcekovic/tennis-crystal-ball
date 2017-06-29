package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class SeasonDoubleRecordDetail extends SimpleRecordDetail<Double> {

	private final int season;

	public SeasonDoubleRecordDetail(
		@JsonProperty("value") double value,
		@JsonProperty("season") int season
	) {
		super(value);
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	@Override public String toDetailString() {
		return format("%1$d", season);
	}
}
