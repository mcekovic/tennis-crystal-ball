package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.records.*;

public abstract class SimpleRecordDetail<T> implements RecordDetail<T> {

	private final T value;

	protected SimpleRecordDetail(T value) {
		this.value = value;
	}

	@Override public T getValue() {
		return value;
	}

	@Override public String toDetailString() {
		return "";
	}
}
