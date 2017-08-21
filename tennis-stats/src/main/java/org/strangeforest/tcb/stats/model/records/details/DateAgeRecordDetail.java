package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

import static java.lang.String.*;

public class DateAgeRecordDetail extends SimpleRecordDetail<String> {

	private final LocalDate date;

	public DateAgeRecordDetail(
		@JsonProperty("value") String value,
		@JsonProperty("date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate date
	) {
		super(value);
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY", date);
	}
}
