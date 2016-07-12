package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonTwoIntegersRecordDetail extends SeasonIntegerRecordDetail {

	private final int value2;

	public SeasonTwoIntegersRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("value2") int value2,
      @JsonProperty("season") int season
	) {
		super(value, season);
		this.value2 = value2;
	}

	public int getValue2() {
		return value2;
	}
}
