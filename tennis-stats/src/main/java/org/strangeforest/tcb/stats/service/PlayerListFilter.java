package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

public class PlayerListFilter {

	private final String searchPhrase;

	private static final String SEARCH_CRITERION = " AND (name ILIKE '%' || ? || '%' OR country_id ILIKE '%' || ? || '%')";

	public PlayerListFilter(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
		return criteria.toString();
	}

	public Object[] getParams(Object... extraParams) {
		return paramList(null, extraParams).toArray();
	}

	public Object[] getParamsWithPrefix(Object firstParam, Object... extraParams) {
		return paramList(firstParam, extraParams).toArray();
	}

	private List<Object> paramList(Object firstParam, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		if (firstParam != null)
			params.add(firstParam);
		if (!isNullOrEmpty(searchPhrase)) {
			params.add(searchPhrase);
			params.add(searchPhrase);
		}
		params.addAll(asList(extraParams));
		return params;
	}
}
