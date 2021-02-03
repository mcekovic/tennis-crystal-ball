package org.strangeforest.tcb.stats.model.records;

import static com.google.common.base.Strings.*;

public interface RecordDetail<T> {

	T getValue();
	
	default String toDetailString() {
		return "";
	}

	default String getValueString() {
		var value = String.valueOf(getValue());
		var detail = toDetailString();
		if (!isNullOrEmpty(detail))
			value += " (" + detail + ')';
		return value;
	}
}
