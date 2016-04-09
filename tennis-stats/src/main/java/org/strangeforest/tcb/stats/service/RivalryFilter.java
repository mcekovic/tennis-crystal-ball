package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

public class RivalryFilter {

	private final Range<Integer> seasonRange;
	private final String level;
	private final String surface;

	private static final String LEVEL_CRITERION       = " AND level = :level::tournament_level";
	private static final String SURFACE_CRITERION     = " AND surface = :surface::surface";

	public RivalryFilter(Range<Integer> seasonRange, String level, String surface) {
		this.seasonRange = seasonRange;
		this.level = level;
		this.surface = surface;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendRangeFilter(criteria, seasonRange, "season", "season");
		if (!isNullOrEmpty(level))
			criteria.append(LEVEL_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		return criteria.toString();
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		addRangeParams(params, seasonRange, "season");
		if (!isNullOrEmpty(level))
			params.addValue("level", level);
		if (!isNullOrEmpty(surface))
			params.addValue("surface", surface);
		return params;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryFilter)) return false;
		RivalryFilter filter = (RivalryFilter)o;
		return Objects.equals(seasonRange, filter.seasonRange) && stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface);
	}

	@Override public int hashCode() {
		return Objects.hash(seasonRange, level, surface);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("seasonRange", seasonRange)
			.add("level", level)
			.add("surface", surface)
			.toString();
	}
}