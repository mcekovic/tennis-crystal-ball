package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

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
}
