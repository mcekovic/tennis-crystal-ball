package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

public class RivalryFilter {

	private final Range<Integer> seasonRange;
	private final boolean last52Weeks;
	private final String level;
	private final String surface;
	private final String round;

	private static final String LAST_52_WEEKS_CRITERION = " AND m.date >= current_date - INTERVAL '1 year'";
	private static final String LEVEL_CRITERION         = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION        = " AND level::TEXT IN (:levels)";
	private static final String SURFACE_CRITERION       = " AND m.surface = :surface::surface";
	private static final String SURFACES_CRITERION      = " AND m.surface::TEXT IN (:surfaces)";
	private static final String ROUND_CRITERION         = " AND round %1$s :round::match_round";

	private static final int LAST_52_WEEKS_SEASON = -1;

	public RivalryFilter(Range<Integer> seasonRange, String level, String surface, String round) {
		this.seasonRange = seasonRange;
		last52Weeks = false;
		this.level = level;
		this.surface = surface;
		this.round = round;
	}

	public RivalryFilter(Integer season, String level, String surface, String round) {
		this.seasonRange = season != null && season != LAST_52_WEEKS_SEASON ? Range.singleton(season) : Range.all();
		last52Weeks = season != null && season == LAST_52_WEEKS_SEASON;
		this.level = level;
		this.surface = surface;
		this.round = round;
	}

	public Range<Integer> getSeasonRange() {
		return seasonRange;
	}

	public boolean isLast52Weeks() {
		return last52Weeks;
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

	public boolean hasSeason() {
		return !seasonRange.equals(Range.all());
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
		if (last52Weeks)
			criteria.append(LAST_52_WEEKS_CRITERION);
		if (!isNullOrEmpty(level))
			criteria.append(level.length() == 1 ? LEVEL_CRITERION : LEVELS_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(surface.length() == 1 ? SURFACE_CRITERION : SURFACES_CRITERION);
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
		if (!isNullOrEmpty(level)) {
			if (level.length() == 1)
				params.addValue("level", level);
			else
				params.addValue("levels", asList(level.split("")));
		}
		if (!isNullOrEmpty(surface)) {
			if (surface.length() == 1)
				params.addValue("surface", surface);
			else
				params.addValue("surfaces", asList(surface.split("")));
		}
		if (!isNullOrEmpty(round))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryFilter)) return false;
		RivalryFilter filter = (RivalryFilter)o;
		return Objects.equals(seasonRange, filter.seasonRange) && last52Weeks == filter.last52Weeks && stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface) && stringsEqual(round, filter.round);
	}

	@Override public int hashCode() {
		return Objects.hash(seasonRange, last52Weeks, emptyToNull(level), emptyToNull(surface), emptyToNull(round));
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("seasonRange", seasonRange.equals(Range.all()) ? null : seasonRange)
			.add("last52Weeks", last52Weeks ? true : null)
			.add("level", emptyToNull(level))
			.add("surface", emptyToNull(surface))
			.add("round", emptyToNull(round))
			.toString();
	}
}