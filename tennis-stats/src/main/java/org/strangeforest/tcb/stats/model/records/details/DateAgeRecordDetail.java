package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

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
}
