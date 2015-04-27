package org.strangeforest.tcb.stats.model;

public class AutocompleteOption {

	private final String id;
	private final String value;
	private final String label;

	public AutocompleteOption(String id, String value, String label) {
		this.id = id;
		this.value = value;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}
}
