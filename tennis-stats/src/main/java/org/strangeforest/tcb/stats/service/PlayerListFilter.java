package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class PlayerListFilter {

	private final Boolean active;
	private final String searchPhrase;

	private static final String ACTIVE_CRITERION = " AND active = :active";
	private static final String SEARCH_CRITERION = " AND (name ILIKE '%' || :searchPhrase || '%' OR country_id ILIKE '%' || :searchPhrase || '%')";

	public PlayerListFilter(String searchPhrase) {
		this(null, searchPhrase);
	}

	public PlayerListFilter(Boolean active, String searchPhrase) {
		this.active = active;
		this.searchPhrase = searchPhrase;
	}

	public boolean hasActive() {
		return active != null;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	protected void appendCriteria(StringBuilder criteria) {
		if (active != null)
			criteria.append(ACTIVE_CRITERION);
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		addParams(params);
		return params;
	}

	protected void addParams(MapSqlParameterSource params) {
		if (active != null)
			params.addValue("active", active);
		if (!isNullOrEmpty(searchPhrase))
			params.addValue("searchPhrase", searchPhrase);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlayerListFilter)) return false;
		PlayerListFilter filter = (PlayerListFilter)o;
		return Objects.equals(active, filter.active) && stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(active, searchPhrase);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("active", active)
			.add("searchPhrase", searchPhrase)
			.toString();
	}
}
