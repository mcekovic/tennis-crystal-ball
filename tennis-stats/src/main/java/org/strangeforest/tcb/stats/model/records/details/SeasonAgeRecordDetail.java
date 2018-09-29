package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonAgeRecordDetail extends SimpleRecordDetail<String> implements SeasonRecordDetail<String> {

	private final int season;

	public SeasonAgeRecordDetail(
		@JsonProperty("value") String value,
		@JsonProperty("season") int season
	) {
		super(value);
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	@Override public String toDetailString() {
		return String.valueOf(season);
	}
}
