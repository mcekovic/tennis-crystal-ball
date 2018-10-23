package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

public class RivalryPlayerListFilter extends PlayerListFilter {

	public static final RivalryPlayerListFilter ALL = new RivalryPlayerListFilter(null, RivalryFilter.ALL);

	private final RivalryFilter rivalryFilter;

	public RivalryPlayerListFilter(String searchPhrase, RivalryFilter rivalryFilter) {
		super(searchPhrase);
		this.rivalryFilter = rivalryFilter;
	}

	public RivalryFilter getRivalryFilter() {
		return rivalryFilter;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		rivalryFilter.appendCriteria(criteria);
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		rivalryFilter.addParams(params);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryPlayerListFilter)) return false;
		if (!super.equals(o)) return false;
		RivalryPlayerListFilter filter = (RivalryPlayerListFilter)o;
		return Objects.equals(rivalryFilter, filter.rivalryFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), rivalryFilter);
	}

	@Override protected MoreObjects.ToStringHelper toStringHelper() {
		return super.toStringHelper().add("rivalryFilter", rivalryFilter);
	}
}
