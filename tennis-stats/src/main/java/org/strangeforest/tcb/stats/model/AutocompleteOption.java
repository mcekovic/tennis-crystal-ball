package org.strangeforest.tcb.stats.model;

import static com.google.common.base.MoreObjects.*;
import static java.util.Objects.*;

public class AutocompleteOption {

	private final String id;
	private final String value;
	private final String label;

	public AutocompleteOption(String id, String value, String label) {
		this.id = requireNonNull(id);
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

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AutocompleteOption)) return false;
		var that = (AutocompleteOption)o;
		return id.equals(that.id);
	}

	@Override public int hashCode() {
		return id.hashCode();
	}

	@Override public String toString() {
		return toStringHelper(this).omitNullValues()
			.add("id", id)
			.add("value", value)
			.toString();
	}
}
