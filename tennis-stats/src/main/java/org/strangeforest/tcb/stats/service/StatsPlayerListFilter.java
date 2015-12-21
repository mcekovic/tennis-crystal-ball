package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class StatsPlayerListFilter extends PlayerListFilter {

	private final Integer season;
	private final String surface;

	private static final String SEASON_CRITERION  = " AND season = ?";
	private static final String SURFACE_CRITERION = " AND surface = ?::surface";

	public StatsPlayerListFilter(Integer season) {
		this(null, season);
	}

	public StatsPlayerListFilter(String searchPhrase, Integer season) {
		this(searchPhrase, season, null);
	}

	public StatsPlayerListFilter(Integer season, String surface) {
		this(null, season, surface);
	}

	public StatsPlayerListFilter(String searchPhrase, Integer season, String surface) {
		super(searchPhrase);
		this.season = season;
		this.surface = surface;
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		super.appendCriteria(criteria);
	}

	@Override protected void addParams(List<Object> params) {
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		super.addParams(params);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StatsPlayerListFilter)) return false;
		if (!super.equals(o)) return false;
		StatsPlayerListFilter filter = (StatsPlayerListFilter)o;
		return Objects.equals(season, filter.season) &&	stringsEqual(surface, filter.surface);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, surface);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("season", season)
			.add("surface", surface)
			.toString();
	}
}
