package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

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

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toDetailString() {
		return format("%3$d %1$d/%2$d", wonLost.getWon(), wonLost.getTotal(), getSeason());
	}
}
