package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class DateAgeRecordDetail implements RecordDetail {

	private final String age;
	private final Date date;

	public DateAgeRecordDetail(
		@JsonProperty("age") String age,
		@JsonProperty("date") Date date
	) {
		this.age = age;
		this.date = date;
	}

	public String getAge() {
		return age;
	}

	public Date getDate() {
		return date;
	}

	@Override public String toString() {
		return format("%1$s (%2$td-%2$tm-%2$tY)", age, date);
	}
}
