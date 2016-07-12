package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;

import static com.google.common.base.Strings.*;

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
		if (!(isNullOrEmpty(category) || Objects.equals(record.getCategory(), category)))
			return false;
		if (!(isNullOrEmpty(searchPhrase) || matches(record.getId()) || matches(record.getName())))
			return false;
		return record.isInfamous() == infamous;
	}

	private boolean matches(String s) {
	 	return !isNullOrEmpty(s) && s.toLowerCase().contains(searchPhrase);
	}
}
