package org.strangeforest.tcb.stats.model.table;

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
}
