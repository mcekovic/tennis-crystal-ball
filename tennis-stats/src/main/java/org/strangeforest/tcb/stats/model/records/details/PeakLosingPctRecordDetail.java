package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

import static java.lang.String.*;

public class PeakLosingPctRecordDetail extends PeakWonLostRecordDetail {

	public PeakLosingPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate date
	) {
		super(won, lost, date);
	}

	@Override public String getValue() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%3$td-%3$tm-%3$tY %1$d/%2$d", wonLost.getLost(), wonLost.getTotal(), getDate());
	}
}
