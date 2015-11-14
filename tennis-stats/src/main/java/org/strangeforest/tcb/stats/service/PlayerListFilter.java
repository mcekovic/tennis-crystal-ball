package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;
import static java.util.Arrays.*;

public class PlayerListFilter {

	private final String searchPhrase;

	private static final String SEARCH_CRITERION = " AND (name ILIKE '%' || ? || '%' OR country_id ILIKE '%' || ? || '%')";

	public PlayerListFilter(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	protected void appendCriteria(StringBuilder criteria) {
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
	}

	public Object[] getParamsWithPrefix(Object firstParam, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		params.add(firstParam);
		addParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	public Object[] getParamsWithPrefixes(Collection prefixParams, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		params.addAll(prefixParams);
		addParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	protected void addParams(List<Object> params) {
		if (!isNullOrEmpty(searchPhrase)) {
			params.add(searchPhrase);
			params.add(searchPhrase);
		}
	}
}
