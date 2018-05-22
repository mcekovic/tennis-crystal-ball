package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class IntegerDoubleRecordDetail extends SimpleRecordDetail<Double> {

	private final int intValue;

	public IntegerDoubleRecordDetail(
		@JsonProperty("value") double value,
		@JsonProperty("int_value") int intValue
	) {
		super(value);
		this.intValue = intValue;
	}

	public int getIntValue() {
		return intValue;
	}

	@Override public String toDetailString() {
		return String.valueOf(intValue);
	}
}
