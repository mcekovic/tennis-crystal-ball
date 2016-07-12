package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class WinningWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public WinningWDrawPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("draw") int draw,
		@JsonProperty("lost") int lost
	) {
		super(won, draw, lost);
	}

	public String getWonLostPct() {
		return wonDrawLost.getWonPctStr(2);
	}

	@Override public String toString() {
		return format("%1$s (%2$d-%3$d-%4$d/%5$d)", getWonLostPct(), wonDrawLost.getWon(), wonDrawLost.getDraw(), wonDrawLost.getLost(), wonDrawLost.getTotal());
	}
}
