package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class DateIntegerRecordDetail extends SimpleRecordDetail<Integer> {

	private final Date date;

	public DateIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("date") Date date
	) {
		super(value);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY", date);
	}
}
