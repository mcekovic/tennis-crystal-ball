package org.strangeforest.tcb.stats.spring;

import java.beans.*;
import java.time.*;

public class DurationPropertyEditor extends PropertyEditorSupport {

	@Override public String getJavaInitializationString() {
		return String.valueOf(getValue());
	}

	@Override public void setAsText(String text) throws IllegalArgumentException {
		super.setValue(Duration.parse(text));
	}
}
