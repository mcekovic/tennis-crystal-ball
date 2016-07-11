package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonTwoIntegersRecordDetail extends IntegerRecordDetail {

	private final int value2;
	private final int season;

	public SeasonTwoIntegersRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("value2") int value2,
      @JsonProperty("season") int season
	) {
		super(value);
		this.value2 = value2;
		this.season = season;
	}

	public int getValue2() {
		return value2;
	}

	public int getSeason() {
		return season;
	}
}
