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

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toString() {
		return format("%1$s (%2$d/%3$d)", getWonLostPct(), wonLost.getWon(), wonLost.getTotal());
	}
}
