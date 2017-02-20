package org.strangeforest.tcb.stats.model;

import static com.google.common.base.MoreObjects.*;

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


	// Object methods
	
	@Override public String toString() {
		return toStringHelper(this).omitNullValues()
			.add("id", id)
			.add("value", value)
			.toString();
	}
}
