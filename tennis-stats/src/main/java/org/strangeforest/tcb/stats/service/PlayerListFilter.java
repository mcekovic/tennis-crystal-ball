package org.strangeforest.tcb.stats.service;

import java.util.ArrayList;
import java.util.*;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class PlayerListFilter {

	private final String searchPhrase;

	private static final String SEARCH_CRITERION = " AND (name ILIKE '%' || :searchPhrase || '%' OR country_id ILIKE '%' || :searchPhrase || '%')";

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

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		addParams(params);
		return params;
	}

	protected void addParams(MapSqlParameterSource params) {
		if (!isNullOrEmpty(searchPhrase))
			params.addValue("searchPhrase", searchPhrase);
	}

	public Object[] getParams(Object... extraParams) {
		List<Object> params = new ArrayList<>();
		addParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
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


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlayerListFilter)) return false;
		PlayerListFilter filter = (PlayerListFilter)o;
		return stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(searchPhrase);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("searchPhrase", searchPhrase)
			.toString();
	}
}
