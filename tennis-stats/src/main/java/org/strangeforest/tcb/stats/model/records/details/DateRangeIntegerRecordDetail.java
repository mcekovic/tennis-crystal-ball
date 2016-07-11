package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

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
}
