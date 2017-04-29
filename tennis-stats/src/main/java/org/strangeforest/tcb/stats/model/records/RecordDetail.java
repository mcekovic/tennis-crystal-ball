package org.strangeforest.tcb.stats.model.records;

public interface RecordDetail<T> {

	T getValue();
	
	default String toDetailString() {
		return "";
	}
}
