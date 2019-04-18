package org.strangeforest.tcb.stats.model.records;

import static com.google.common.base.Strings.*;

public interface RecordDetail<T> {

	T getValue();
	
	default String toDetailString() {
		return "";
	}

	default String getValueString() {
		String value = String.valueOf(getValue());
		String detail = toDetailString();
		if (!isNullOrEmpty(detail))
			value += " (" + detail + ')';
		return value;
	}
}
