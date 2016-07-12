package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class IntegerRecordDetail extends SimpleRecordDetail<Integer> {

	public IntegerRecordDetail(
		@JsonProperty("value") int value
	) {
		super(value);
	}
}
