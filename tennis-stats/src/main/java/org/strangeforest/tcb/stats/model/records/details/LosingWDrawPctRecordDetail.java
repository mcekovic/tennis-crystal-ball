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

	@Override public String getValue() {
		return wonDrawLost.inverted().getWonPctStr(2);
	}

	@Override public String toDetailString() {
		return format("%1$d-%2$d-%3$d/%4$d", wonDrawLost.getLost(), wonDrawLost.getDraw(), wonDrawLost.getWon(), wonDrawLost.getTotal());
	}
}
