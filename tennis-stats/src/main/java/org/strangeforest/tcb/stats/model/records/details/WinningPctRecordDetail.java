package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class WinningPctRecordDetail extends WonLostRecordDetail {

	public WinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost
	) {
		super(won, lost);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toDetailString() {
		return format("%1$d/%2$d", wonLost.getWon(), wonLost.getTotal());
	}
}
