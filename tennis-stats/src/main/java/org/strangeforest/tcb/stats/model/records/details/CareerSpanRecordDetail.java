package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

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

	@Override public String toString() {
		return format("%1$s (%2$td-%2$tm-%2$tY-%3$td-%3$tm-%3$tY)", span, startDate, endDate);
	}
}
