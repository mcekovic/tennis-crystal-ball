package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

public class CareerSpanRecordDetail implements RecordDetail {

	private final String span;
	private final Date startDate;
	private final Date endDate;

	public CareerSpanRecordDetail(
		@JsonProperty("span") String span,
		@JsonProperty("start_date") Date startDate,
		@JsonProperty("end_date") Date endDate
	) {
		this.span = span;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String getSpan() {
		return span;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
