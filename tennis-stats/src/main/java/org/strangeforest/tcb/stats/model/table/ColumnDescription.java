package org.strangeforest.tcb.stats.model.table;

import static com.google.common.base.MoreObjects.toStringHelper;

public class ColumnDescription {

	private final String type;
	private final String label;

	public ColumnDescription(String type, String label) {
		this.type = type;
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}


	// Object methods

	@Override public String toString() {
		return toStringHelper(this).omitNullValues()
			.add("type", type)
			.add("label", label)
			.toString();
	}
}
