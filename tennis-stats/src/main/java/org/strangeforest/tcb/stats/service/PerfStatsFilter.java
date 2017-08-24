package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class PerfStatsFilter extends PlayerListFilter {

	// Factory

	public static final PerfStatsFilter ALL = new PerfStatsFilter(null, null, null, null, null, null);

 	public static PerfStatsFilter forSeason(Integer season) {
		return new PerfStatsFilter(season, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndLevel(Integer season, String level) {
		return new PerfStatsFilter(season, level, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndSurface(Integer season, String surface) {
		return new PerfStatsFilter(season, null, surface, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndRound(Integer season, String round) {
		return new PerfStatsFilter(season, null, null, round, null, null);
	}

	public static PerfStatsFilter forSeasonAndOpponent(Integer season, String opponent) {
		return new PerfStatsFilter(season, null, null, null, null, OpponentFilter.forStats(opponent, null));
	}

 	public static PerfStatsFilter forLevel(String level) {
		return new PerfStatsFilter(null, level, null, null, null, null);
	}

 	public static PerfStatsFilter forLevelAndTournament(String level, Integer tournamentId) {
		return new PerfStatsFilter(null, level, null, null, tournamentId, null);
	}

 	public static PerfStatsFilter forSurface(String surface) {
		return new PerfStatsFilter(null, null, surface, null, null, null);
	}

 	public static PerfStatsFilter forSurfaceAndTournament(String surface, Integer tournamentId) {
		return new PerfStatsFilter(null, null, surface, null, tournamentId, null);
	}

 	public static PerfStatsFilter forRound(String round) {
		return new PerfStatsFilter(null, null, null, round, null, null);
	}

 	public static PerfStatsFilter forRoundAndTournament(String round, Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, round, tournamentId, null);
	}

 	public static PerfStatsFilter forTournament(Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, tournamentId, null);
	}

	public static PerfStatsFilter forOpponent(String opponent) {
		return new PerfStatsFilter(null, null, null, null, null, OpponentFilter.forStats(opponent, null));
	}

	public static PerfStatsFilter forOpponentAndTournament(String opponent, Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, tournamentId, OpponentFilter.forStats(opponent, null));
	}


	// Instance

	private final Integer season;
	private final boolean last52Weeks;
	private final String level;
	private final String surface;
	private final String round;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final OpponentFilter opponentFilter;

	private static final String SEASON_CRITERION           = " AND season = :season";
	private static final String LAST_52_WEEKS_CRITERION    = " AND date >= current_date - INTERVAL '1 year'";
	private static final String LEVEL_CRITERION            = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION           = " AND level::TEXT IN (:levels)";
	private static final String SURFACE_CRITERION          = " AND surface = :surface::surface";
	private static final String SURFACES_CRITERION         = " AND surface::TEXT IN (:surfaces)";
	private static final String ROUND_CRITERION            = " AND round %1$s :round::match_round AND level NOT IN ('D', 'T')";
	private static final String TOURNAMENT_CRITERION       = " AND tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND tournament_event_id = :tournamentEventId";

	private static final int LAST_52_WEEKS_SEASON = -1;

	public PerfStatsFilter(Integer season, String level, String surface, String round, Integer tournamentId, OpponentFilter opponentFilter) {
		this(null, null, season, level, surface, round, tournamentId, null, opponentFilter);
	}

	public PerfStatsFilter(Integer season, String level, String surface, String round, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter) {
		this(null, null, season, level, surface, round, tournamentId, tournamentEventId, opponentFilter);
	}

	public PerfStatsFilter(Boolean active, String searchPhrase, Integer season, String level, String surface, String round, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter) {
		super(active, searchPhrase);
		this.season = season != null && season != LAST_52_WEEKS_SEASON ? season : null;
		last52Weeks = season != null && season == LAST_52_WEEKS_SEASON;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
	}

	public String getBaseCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendBaseCriteria(criteria);
		return criteria.toString();
	}

	public String getSearchCriteria() {
		StringBuilder criteria = new StringBuilder();
		super.appendCriteria(criteria);
		return criteria.toString();
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		appendBaseCriteria(criteria);
		super.appendCriteria(criteria);
	}

	private void appendBaseCriteria(StringBuilder criteria) {
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (last52Weeks)
			criteria.append(LAST_52_WEEKS_CRITERION);
		if (!isNullOrEmpty(level))
			criteria.append(level.length() == 1 ? LEVEL_CRITERION : LEVELS_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(surface.length() == 1 ? SURFACE_CRITERION : SURFACES_CRITERION);
		if (!isNullOrEmpty(round))
			criteria.append(format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (tournamentEventId != null)
			criteria.append(TOURNAMENT_EVENT_CRITERION);
		opponentFilter.appendCriteria(criteria);
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (season != null)
			params.addValue("season", season);
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
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
		if (tournamentEventId != null)
			params.addValue("tournamentEventId", tournamentEventId);
		opponentFilter.addParams(params);
	}

	public Integer getSeason() {
		return season;
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

	public OpponentFilter getOpponentFilter() {
		return opponentFilter;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public boolean isTimeLocalized() {
		return season != null || last52Weeks;
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasSurfaceGroup() {
		return hasSurface() && surface.length() > 1;
	}

	public boolean hasRound() {
		return !isNullOrEmpty(round);
	}

	public boolean hasTournament() {
		return tournamentId != null;
	}

	public boolean hasTournamentEvent() {
		return tournamentEventId != null;
	}

	public boolean hasOpponent() {
		return !opponentFilter.isEmpty();
	}

	public boolean isForSeason() {
		return season != null && equals(forSeason(season));
	}

	public boolean isForSeasonAndLevel() {
		return season != null && !isNullOrEmpty(level) && equals(forSeasonAndLevel(season, level));
	}

	public boolean isForSeasonAndSurface() {
		return season != null && !isNullOrEmpty(surface) && equals(forSeasonAndSurface(season, surface));
	}

	public boolean isForSeasonAndRound() {
		return season != null && !isNullOrEmpty(round) && equals(forSeasonAndRound(season, round));
	}

	public boolean isForSeasonAndOpposition() {
		return season != null && opponentFilter.getOpponent() != null && equals(forSeasonAndOpponent(season, opponentFilter.getOpponent().name()));
	}

	public boolean isForLevel() {
		return !isNullOrEmpty(level) && equals(forLevel(level));
	}

	public boolean isForLevelAndTournament() {
		return !isNullOrEmpty(level) && tournamentId != null && equals(forLevelAndTournament(level, tournamentId));
	}

	public boolean isForSurface() {
		return !isNullOrEmpty(surface) && equals(forSurface(surface));
	}

	public boolean isForRound() {
		return !isNullOrEmpty(round) && equals(forRound(round));
	}

	public boolean isForRoundAndTournament() {
		return !isNullOrEmpty(round) && tournamentId != null && equals(forRoundAndTournament(round, tournamentId));
	}

	public boolean isForTournament() {
		return tournamentId != null && equals(forTournament(tournamentId));
	}

	public boolean isForOpposition() {
		return opponentFilter.getOpponent() != null && equals(forOpponent(opponentFilter.getOpponent().name()));
	}

	public boolean isForOppositionAndTournament() {
		return opponentFilter.getOpponent() != null && tournamentId != null && equals(forOpponentAndTournament(opponentFilter.getOpponent().name(), tournamentId));
	}

	public boolean isTournamentGranularity() {
		return isNullOrEmpty(round) && opponentFilter.isEmpty();
	}

	public boolean isEmpty() {
		return season == null && !last52Weeks && isNullOrEmpty(level) && isNullOrEmpty(surface) && isNullOrEmpty(round) && tournamentId == null && tournamentEventId == null && opponentFilter.isEmpty();
	}

	public boolean isEmptyOrForSeasonOrSurface() {
		return !last52Weeks && isNullOrEmpty(level) && isNullOrEmpty(round) && tournamentId == null && tournamentEventId == null && opponentFilter.isEmpty();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PerfStatsFilter)) return false;
		if (!super.equals(o)) return false;
		PerfStatsFilter filter = (PerfStatsFilter)o;
		return Objects.equals(season, filter.season) &&	last52Weeks == filter.last52Weeks && stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface) && stringsEqual(round, filter.round)
		    && Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId) && opponentFilter.equals(filter.opponentFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, last52Weeks, emptyToNull(level), emptyToNull(surface), emptyToNull(round), tournamentId, tournamentEventId, opponentFilter);
	}

	@Override protected MoreObjects.ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("season", season)
			.add("last52Weeks", last52Weeks ? true : null)
			.add("level", emptyToNull(level))
			.add("surface", emptyToNull(surface))
			.add("round", emptyToNull(round))
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("opponentFilter", opponentFilter);
	}
}
