package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class SeasonRangeIntegerRecordDetail extends IntegerRecordDetail {

	private final int startSeason;
	private final int endSeason;

	public SeasonRangeIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("start_season") int startSeason,
		@JsonProperty("end_season") int endSeason
	) {
		super(value);
		this.startSeason = startSeason;
		this.endSeason = endSeason;
	}

	public int getStartSeason() {
		return startSeason;
	}

	public int getEndSeason() {
		return endSeason;
	}

	@Override public String toDetailString() {
		return format("%1$d-%2$d", startSeason, endSeason);
	}
}
