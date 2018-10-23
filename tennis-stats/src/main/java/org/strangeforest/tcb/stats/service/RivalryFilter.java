package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;
import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class RivalryFilter {

	public static final RivalryFilter ALL = new RivalryFilter(null, null, null, null, null, null, null, null, null);

	private final Range<Integer> seasonRange;
	private final boolean last52Weeks;
	private final String level;
	private final Integer bestOf;
	private final String surface;
	private final Boolean indoor;
	private final Range<Integer> speedRange;
	private final String round;
	private final Integer tournamentId;

	private static final String LAST_52_WEEKS_CRITERION = " AND m.date >= current_date - INTERVAL '1 year'";
	private static final String LEVEL_CRITERION         = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION        = " AND level::TEXT IN (:levels)";
	private static final String BEST_OF_CRITERION       = " AND best_of = :bestOf";
	private static final String SURFACE_CRITERION       = " AND m.surface = :surface::surface";
	private static final String SURFACES_CRITERION      = " AND m.surface::TEXT IN (:surfaces)";
	private static final String INDOOR_CRITERION        = " AND m.indoor = :indoor";
	private static final String ROUND_CRITERION         = " AND round %1$s :round::match_round";
	private static final String ENTRY_ROUND_CRITERION   = " AND round BETWEEN 'R128' AND 'R16'";
	private static final String TOURNAMENT_CRITERION    = " AND tournament_id = :tournamentId";

	private static final int LAST_52_WEEKS_SEASON = -1;

	public RivalryFilter(Range<Integer> seasonRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, Integer tournamentId) {
		this(null, seasonRange, level, bestOf, surface, indoor, speedRange, round, tournamentId);
	}

	public RivalryFilter(Integer season, Range<Integer> seasonRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, Integer tournamentId) {
		this.seasonRange = season != null && season != LAST_52_WEEKS_SEASON ? Range.singleton(season) : (seasonRange != null ? seasonRange : Range.all());
		last52Weeks = season != null && season == LAST_52_WEEKS_SEASON;
		this.level = level;
		this.bestOf = bestOf;
		this.surface = surface;
		this.indoor = indoor;
		this.speedRange = speedRange != null ? speedRange : Range.all();
		this.round = round;
		this.tournamentId = tournamentId;
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

	public Integer getBestOf() {
		return bestOf;
	}

	public String getSurface() {
		return surface;
	}

	public Boolean getIndoor() {
		return indoor;
	}

	public Range<Integer> getSpeedRange() {
		return speedRange;
	}

	public String getRound() {
		return round;
	}

	public Integer getTournamentId() {
		return tournamentId;
	}

	public boolean hasSeason() {
		return !seasonRange.equals(Range.all());
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasBestOf() {
		return bestOf != null;
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasIndoor() {
		return indoor != null;
	}

	public boolean hasSpeedRange() {
		return !speedRange.equals(Range.all());
	}

	public boolean hasRound() {
		return !isNullOrEmpty(round);
	}

	public boolean hasTournament() {
		return tournamentId != null;
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
		if (bestOf != null)
			criteria.append(BEST_OF_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(surface.length() == 1 ? SURFACE_CRITERION : SURFACES_CRITERION);
		if (indoor != null)
			criteria.append(INDOOR_CRITERION);
		appendRangeFilter(criteria, speedRange, "es.court_speed", "speed");
		if (!isNullOrEmpty(round))
			criteria.append(round.equals(Round.ENTRY.getCode()) ? ENTRY_ROUND_CRITERION : format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
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
		if (bestOf != null)
			params.addValue("bestOf", bestOf);
		if (!isNullOrEmpty(surface)) {
			if (surface.length() == 1)
				params.addValue("surface", surface);
			else
				params.addValue("surfaces", asList(surface.split("")));
		}
		if (indoor != null)
			params.addValue("indoor", indoor);
		addRangeParams(params, speedRange, "speed");
		if (!isNullOrEmpty(round) && !round.equals(Round.ENTRY.getCode()))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryFilter)) return false;
		RivalryFilter filter = (RivalryFilter)o;
		return seasonRange.equals(filter.seasonRange) && last52Weeks == filter.last52Weeks && stringsEqual(level, filter.level) && Objects.equals(bestOf, filter.bestOf) &&
			stringsEqual(surface, filter.surface) && Objects.equals(indoor, filter.indoor) && speedRange.equals(filter.speedRange) && stringsEqual(round, filter.round) && Objects.equals(tournamentId, filter.tournamentId);
	}

	@Override public int hashCode() {
		return Objects.hash(seasonRange, last52Weeks, emptyToNull(level), bestOf, emptyToNull(surface), indoor, speedRange, emptyToNull(round), tournamentId);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("seasonRange", nullIf(seasonRange, Range.all()))
			.add("last52Weeks", nullIf(last52Weeks, true))
			.add("level", emptyToNull(level))
			.add("bestOf", bestOf)
			.add("surface", emptyToNull(surface))
			.add("indoor", indoor)
			.add("speedRange", nullIf(speedRange, Range.all()))
			.add("round", emptyToNull(round))
			.add("tournamentId", tournamentId)
			.toString();
	}
}