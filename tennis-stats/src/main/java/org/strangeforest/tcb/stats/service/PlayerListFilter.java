package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class PlayerListFilter {

	public static final PlayerListFilter ALL = new PlayerListFilter(null);

	private final Boolean active;
	private final Collection<String> countryIds;
	private final String searchPhrase;
	private String prefix = "";

	private static final String ACTIVE_CRITERION = " AND %1$sactive = :active";
	private static final String COUNTRY_CRITERION = " AND %1$scountry_id IN (:countryIds)";
	private static final String SEARCH_CRITERION = " AND (%1$sname ILIKE '%%' || :searchPhrase || '%%' OR %1$scountry_id ILIKE '%%' || :searchPhrase || '%%')";

	public PlayerListFilter(String searchPhrase) {
		this(null, null, searchPhrase);
	}

	public PlayerListFilter(Boolean active, String searchPhrase) {
		this(active, null, searchPhrase);
	}

	public PlayerListFilter(Boolean active, Collection<String> countryIds, String searchPhrase) {
		this.active = active;
		this.countryIds = countryIds != null ? countryIds : Collections.emptyList();
		this.searchPhrase = searchPhrase;
	}

	public boolean hasActive() {
		return active != null;
	}

	public boolean hasSearchPhrase() {
		return !isNullOrEmpty(searchPhrase);
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	protected void appendCriteria(StringBuilder criteria) {
		if (active != null)
			criteria.append(format(ACTIVE_CRITERION, prefix));
		if (!countryIds.isEmpty())
			criteria.append(format(COUNTRY_CRITERION, prefix));
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(format(SEARCH_CRITERION, prefix));
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
		if (!countryIds.isEmpty())
			params.addValue("countryIds", countryIds);
	}

	public PlayerListFilter withPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlayerListFilter)) return false;
		PlayerListFilter filter = (PlayerListFilter)o;
		return Objects.equals(active, filter.active) && countryIds.equals(filter.countryIds) && stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(active, countryIds, emptyToNull(searchPhrase));
	}

	@Override public final String toString() {
		return toStringHelper().toString();
	}

	protected MoreObjects.ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("active", active)
			.add("countryIds", countryIds.isEmpty() ? null : countryIds)
			.add("searchPhrase", emptyToNull(searchPhrase));
	}
}
