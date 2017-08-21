package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

import static java.lang.String.*;

public class CareerSpanRecordDetail extends SimpleRecordDetail<String> {

	private final LocalDate startDate;
	private final LocalDate endDate;

	public CareerSpanRecordDetail(
		@JsonProperty("value") String value,
		@JsonProperty("start_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate startDate,
		@JsonProperty("end_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate endDate
	) {
		super(value);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY - %2$td-%2$tm-%2$tY", startDate, endDate);
	}
}
