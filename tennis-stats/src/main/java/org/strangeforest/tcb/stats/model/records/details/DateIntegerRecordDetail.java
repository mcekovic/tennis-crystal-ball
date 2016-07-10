package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class DateIntegerRecordDetail extends IntegerRecordDetail {

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
}
