package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonWinningPctRecordDetail extends SeasonWonLostRecordDetail {

	public SeasonWinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("season") int season
	) {
		super(won, lost, season);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}
}
