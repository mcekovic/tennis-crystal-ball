package org.strangeforest.tcb.stats.service;

import java.time.*;
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

public class PerfStatsFilter extends PlayerListFilter {

	// Factory

	public static final PerfStatsFilter ALL = new PerfStatsFilter(null, null, null, null, null, null, null, null, null, null, null);

 	public static PerfStatsFilter forSeason(Integer season) {
		return new PerfStatsFilter(season, null, null, null, null, null, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndLevel(Integer season, String level) {
		return new PerfStatsFilter(season, null, level, null, null, null, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndBestOf(Integer season, Integer bestOf) {
		return new PerfStatsFilter(season, null, null, bestOf, null, null, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndSurface(Integer season, String surface) {
		return new PerfStatsFilter(season, null, null, null, surface, null, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndIndoor(Integer season, Boolean indoor) {
		return new PerfStatsFilter(season, null, null, null, null, indoor, null, null, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndRound(Integer season, String round) {
		return new PerfStatsFilter(season, null, null, null, null, null, null, round, null, null, null);
	}

	public static PerfStatsFilter forSeasonAndOpponent(Integer season, String opponent) {
		return new PerfStatsFilter(season, null, null, null, null, null, null, null, null, null, OpponentFilter.forStats(opponent, null));
	}

 	public static PerfStatsFilter forLevel(String level) {
		return new PerfStatsFilter(null, null, level, null, null, null, null, null, null, null, null);
	}

 	public static PerfStatsFilter forLevelAndTournament(String level, Integer tournamentId) {
		return new PerfStatsFilter(null, null, level, null, null, null, null, null, null, tournamentId, null);
	}

	public static PerfStatsFilter forBestOf(Integer bestOf) {
		return new PerfStatsFilter(null, null, null, bestOf, null, null, null, null, null, null, null);
	}

 	public static PerfStatsFilter forSurface(String surface) {
		return new PerfStatsFilter(null, null, null, null, surface, null, null, null, null, null, null);
	}

 	public static PerfStatsFilter forSurfaceAndTournament(String surface, Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, surface, null, null, null, null, tournamentId, null);
	}

	public static PerfStatsFilter forIndoor(Boolean indoor) {
		return new PerfStatsFilter(null, null, null, null, null, indoor, null, null, null, null, null);
	}

 	public static PerfStatsFilter forRound(String round) {
		return new PerfStatsFilter(null, null, null, null, null, null, null, round, null, null, null);
	}

 	public static PerfStatsFilter forRoundAndTournament(String round, Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, null, null, null, round, null, tournamentId, null);
	}

 	public static PerfStatsFilter forTournament(Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, null, null, null, null, null, tournamentId, null);
	}

	public static PerfStatsFilter forOpponent(String opponent) {
		return new PerfStatsFilter(null, null, null, null, null, null, null, null, null, null, OpponentFilter.forStats(opponent, null));
	}

	public static PerfStatsFilter forOpponentAndTournament(String opponent, Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, null, null, null, null, null, tournamentId, OpponentFilter.forStats(opponent, null));
	}


	// Instance

	private final Integer season;
	private final boolean last52Weeks;
	private final Range<LocalDate> dateRange;
	private final String level;
	private final Integer bestOf;
	private final String surface;
	private final Boolean indoor;
	private final Range<Integer> speedRange;
	private final String round;
	private final String result;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final OpponentFilter opponentFilter;

	private static final String SEASON_CRITERION           = " AND season = :season";
	private static final String LAST_52_WEEKS_CRITERION    = " AND date >= current_date - INTERVAL '1 year'";
	private static final String LEVEL_CRITERION            = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION           = " AND level::TEXT IN (:levels)";
	private static final String BEST_OF_CRITERION          = " AND best_of = :bestOf";
	private static final String SURFACE_CRITERION          = " AND surface = :surface::surface";
	private static final String SURFACES_CRITERION         = " AND surface::TEXT IN (:surfaces)";
	private static final String INDOOR_CRITERION           = " AND indoor = :indoor";
	private static final String ROUND_CRITERION            = " AND round %1$s :round::match_round AND level NOT IN ('D', 'T')";
	private static final String ENTRY_ROUND_CRITERION      = " AND round BETWEEN 'R128' AND 'R16' AND level NOT IN ('D', 'T')";
	private static final String RESULT_CRITERION           = " AND r.result %1$s :result::tournament_event_result AND level NOT IN ('D', 'T')";
	private static final String TOURNAMENT_CRITERION       = " AND tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND tournament_event_id = :tournamentEventId";

	private static final int LAST_52_WEEKS_SEASON = -1;

	public PerfStatsFilter(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, OpponentFilter opponentFilter) {
		this(null, null, season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, null, opponentFilter);
	}

	public PerfStatsFilter(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter) {
		this(null, null, season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter);
	}

	public PerfStatsFilter(Boolean active, String searchPhrase, Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter) {
		super(active, searchPhrase);
		this.season = season != null && season != LAST_52_WEEKS_SEASON ? season : null;
		last52Weeks = season != null && season == LAST_52_WEEKS_SEASON;
		this.dateRange = dateRange != null ? dateRange : Range.all();
		this.level = level;
		this.bestOf = bestOf;
		this.surface = surface;
		this.indoor = indoor;
		this.speedRange = speedRange != null ? speedRange : Range.all();
		this.round = round;
		this.result = result;
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
		appendRangeFilter(criteria, dateRange, "date", "date");
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
		if (!isNullOrEmpty(result))
			criteria.append(format(RESULT_CRITERION, result.endsWith("+") ? ">=" : "="));
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
		addRangeParams(params, dateRange, "date");
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
		if (!isNullOrEmpty(result))
			params.addValue("result", result.endsWith("+") ? result.substring(0, result.length() - 1) : result);
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

	public Range<LocalDate> getDateRange() {
		return dateRange;
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

	public String getResult() {
		return result;
	}

	public OpponentFilter getOpponentFilter() {
		return opponentFilter;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public boolean isTimeLocalized() {
		return season != null || last52Weeks || !dateRange.equals(Range.all());
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasDateRange() {
		return !dateRange.equals(Range.all());
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

	public boolean hasSurfaceGroup() {
		return hasSurface() && surface.length() > 1;
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

	public boolean hasResult() {
		return !isNullOrEmpty(result);
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

	public boolean isForSeasonAndBestOf() {
		return season != null && bestOf != null && equals(forSeasonAndBestOf(season, bestOf));
	}

	public boolean isForSeasonAndSurface() {
		return season != null && !isNullOrEmpty(surface) && equals(forSeasonAndSurface(season, surface));
	}

	public boolean isForSeasonAndIndoor() {
		return season != null && indoor != null && equals(forSeasonAndIndoor(season, indoor));
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

	public boolean isForBestOf() {
		return bestOf != null && equals(forBestOf(bestOf));
	}

	public boolean isForSurface() {
		return !isNullOrEmpty(surface) && equals(forSurface(surface));
	}

	public boolean isForIndoor() {
		return indoor != null && equals(forIndoor(indoor));
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

	public boolean isTournamentEventGranularity() {
		return bestOf == null && isNullOrEmpty(round) && opponentFilter.isEmpty();
	}

	public boolean isEmpty() {
		return season == null && !last52Weeks && dateRange.equals(Range.all()) &&
			isNullOrEmpty(level) && bestOf == null && isNullOrEmpty(surface) && indoor == null && isNullOrEmpty(round) && speedRange.equals(Range.all()) &&
			isNullOrEmpty(result) && tournamentId == null && tournamentEventId == null && opponentFilter.isEmpty();
	}

	public boolean isEmptyOrForSeasonOrSurface() {
		return !last52Weeks && dateRange.equals(Range.all()) &&
		   isNullOrEmpty(level) && bestOf == null && indoor == null && speedRange.equals(Range.all()) && isNullOrEmpty(round) && isNullOrEmpty(result) &&
			tournamentId == null && tournamentEventId == null && opponentFilter.isEmpty();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PerfStatsFilter)) return false;
		if (!super.equals(o)) return false;
		PerfStatsFilter filter = (PerfStatsFilter)o;
		return Objects.equals(season, filter.season) &&	last52Weeks == filter.last52Weeks && dateRange.equals(filter.dateRange) &&
			stringsEqual(level, filter.level) && Objects.equals(bestOf, filter.bestOf) && stringsEqual(surface, filter.surface) && Objects.equals(indoor, filter.indoor) && speedRange.equals(filter.speedRange) &&
			stringsEqual(round, filter.round) && stringsEqual(result, filter.result) && Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId) && opponentFilter.equals(filter.opponentFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, last52Weeks, dateRange, emptyToNull(level), bestOf, emptyToNull(surface), indoor, speedRange, emptyToNull(round), emptyToNull(result), tournamentId, tournamentEventId, opponentFilter);
	}

	@Override protected MoreObjects.ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("season", season)
			.add("last52Weeks", nullIf(last52Weeks, true))
			.add("dateRange", nullIf(dateRange, Range.all()))
			.add("level", emptyToNull(level))
			.add("bestOf", bestOf)
			.add("surface", emptyToNull(surface))
			.add("indoor", indoor)
			.add("speedRange", nullIf(speedRange, Range.all()))
			.add("round", emptyToNull(round))
			.add("result", emptyToNull(result))
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("opponentFilter", opponentFilter);
	}
}
