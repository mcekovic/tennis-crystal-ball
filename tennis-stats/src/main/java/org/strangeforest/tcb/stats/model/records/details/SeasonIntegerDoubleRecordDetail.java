package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class SeasonIntegerDoubleRecordDetail extends IntegerDoubleRecordDetail implements SeasonRecordDetail<Double> {

	private final int season;

	public SeasonIntegerDoubleRecordDetail(
		@JsonProperty("value") double value,
		@JsonProperty("int_value") int intValue,
		@JsonProperty("season") int season
	) {
		super(value, intValue);
		this.season = season;
	}

	@Override public int getSeason() {
		return season;
	}

	@Override public String toDetailString() {
		return format("%1$d - %2$d", season, getIntValue());
	}
}
