package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class LosingWDrawPctRecordDetail extends WonDrawLostRecordDetail {

	public LosingWDrawPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("draw") int draw,
		@JsonProperty("lost") int lost
	) {
		super(won, draw, lost);
	}

	public String getLostWonPct() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}

	@Override public String toString() {
		return format("%1$s (%2$d-%3$d-%4$d/%5$d)", getLostWonPct(), wonDrawLost.getLost(), wonDrawLost.getDraw(), wonDrawLost.getWon(), wonDrawLost.getTotal());
	}
}
