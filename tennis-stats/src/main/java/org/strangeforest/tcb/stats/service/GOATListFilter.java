package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class GOATListFilter {

	private final String searchPhrase;

	private static final String SEARCH_CRITERION = " AND (name ILIKE '%' || ? || '%' OR country_id ILIKE '%' || ? || '%')";

	public GOATListFilter(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
		return criteria.toString();
	}

	public Object[] getParams() {
		return paramList().toArray();
	}

	public Object[] getParams(int offset, int limit) {
		List<Object> params = paramList();
		params.add(offset);
		params.add(limit);
		return params.toArray();
	}

	private List<Object> paramList() {
		List<Object> params = new ArrayList<>();
		if (!isNullOrEmpty(searchPhrase)) {
			params.add(searchPhrase);
			params.add(searchPhrase);
		}
		return params;
	}
}
