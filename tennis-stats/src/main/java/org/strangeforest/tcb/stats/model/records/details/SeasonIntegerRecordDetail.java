package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonIntegerRecordDetail extends SimpleRecordDetail<Integer> implements SeasonRecordDetail<Integer> {

	private final int season;

	public SeasonIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("season") int season
	) {
		super(value);
		this.season = season;
	}

	@Override public int getSeason() {
		return season;
	}

	@Override public String toDetailString() {
		return String.valueOf(season);
	}
}
