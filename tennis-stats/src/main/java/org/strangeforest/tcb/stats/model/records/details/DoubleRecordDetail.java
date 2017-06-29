package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class DoubleRecordDetail extends SimpleRecordDetail<Double> {

	public DoubleRecordDetail(
		@JsonProperty("value") double value
	) {
		super(value);
	}
}
