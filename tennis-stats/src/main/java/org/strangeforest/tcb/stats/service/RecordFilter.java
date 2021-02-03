package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.strangeforest.tcb.stats.model.records.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class RecordFilter {

	private final String category;
	private final String searchPhrase;
	private final boolean infamous;

	public RecordFilter(String category, String searchPhrase, boolean infamous) {
		this.category = category;
		this.searchPhrase = searchPhrase.toLowerCase();
		this.infamous = infamous;
	}

	public String getCategory() {
		return category;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public boolean isInfamous() {
		return infamous;
	}

	public boolean predicate(Record record) {
		if (!(isNullOrEmpty(category) || Objects.equals(record.getCategory().getId(), category)))
			return false;
		if (!(isNullOrEmpty(searchPhrase) || matches(record.getId()) || matches(record.getName())))
			return false;
		return record.isInfamous() == infamous;
	}

	private boolean matches(String s) {
	 	return !isNullOrEmpty(s) && s.toLowerCase().contains(searchPhrase);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RecordFilter)) return false;
		var filter = (RecordFilter)o;
		return stringsEqual(category, filter.category) && stringsEqual(searchPhrase, filter.searchPhrase) && infamous == filter.infamous;
	}

	@Override public int hashCode() {
		return Objects.hash(emptyToNull(category), emptyToNull(searchPhrase), infamous);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("category", emptyToNull(category))
			.add("searchPhrase", emptyToNull(searchPhrase))
			.add("infamous", nullIf(infamous, true))
		.toString();
	}
}
