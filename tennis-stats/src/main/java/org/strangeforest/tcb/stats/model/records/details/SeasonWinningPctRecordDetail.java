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

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toString() {
		return format("%1$s (%2$d/%3$d in %4$d)", getWonLostPct(), wonLost.getWon(), wonLost.getTotal(), getSeason());
	}
}
