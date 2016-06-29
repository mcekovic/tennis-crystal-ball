package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

public class RivalryFilter {

	private final Range<Integer> seasonRange;
	private final String level;
	private final String surface;
	private final String round;

	private static final String LEVEL_CRITERION       = " AND level = :level::tournament_level";
	private static final String SURFACE_CRITERION     = " AND m.surface = :surface::surface";
	private static final String ROUND_CRITERION       = " AND round %1$s :round::match_round";

	public RivalryFilter(Range<Integer> seasonRange, String level, String surface, String round) {
		this.seasonRange = seasonRange;
		this.level = level;
		this.surface = surface;
		this.round = round;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getRound() {
		return round;
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasRound() {
		return !isNullOrEmpty(round);
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	void appendCriteria(StringBuilder criteria) {
		appendRangeFilter(criteria, seasonRange, "season", "season");
		if (!isNullOrEmpty(level))
			criteria.append(LEVEL_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		if (!isNullOrEmpty(round))
			criteria.append(format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		addParams(params);
		return params;
	}

	void addParams(MapSqlParameterSource params) {
		addRangeParams(params, seasonRange, "season");
		if (!isNullOrEmpty(level))
			params.addValue("level", level);
		if (!isNullOrEmpty(surface))
			params.addValue("surface", surface);
		if (!isNullOrEmpty(round))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryFilter)) return false;
		RivalryFilter filter = (RivalryFilter)o;
		return Objects.equals(seasonRange, filter.seasonRange) && stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface) && stringsEqual(round, filter.round);
	}

	@Override public int hashCode() {
		return Objects.hash(seasonRange, level, surface, round);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("seasonRange", seasonRange)
			.add("level", level)
			.add("surface", surface)
			.add("round", round)
			.toString();
	}
}