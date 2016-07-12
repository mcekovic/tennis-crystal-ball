package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class DateRangeIntegerRecordDetail extends IntegerRecordDetail {

	private final Date startDate;
	private final Date endDate;

	public DateRangeIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("start_date") Date startDate,
		@JsonProperty("end_date") Date endDate
	) {
		super(value);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY - %2$td-%2$tm-%2$tY", startDate, endDate);
	}
}
