package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class PeakWinningPctRecordDetail extends PeakWonLostRecordDetail {

	public PeakWinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("date") Date date
	) {
		super(won, lost, date);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toDetailString() {
		return format("%3$td-%3$tm-%3$tY %1$d/%2$d", wonLost.getWon(), wonLost.getTotal(), getDate());
	}
}
