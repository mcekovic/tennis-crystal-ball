package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

public class IntegerRecordDetail implements RecordDetail {

	private final int value;

	public IntegerRecordDetail(
		@JsonProperty("value") int value
	) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override public String toString() {
		return String.valueOf(value);
	}
}
