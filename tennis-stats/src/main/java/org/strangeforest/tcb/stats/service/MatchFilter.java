package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class MatchFilter extends TournamentEventResultFilter {

	// Factory

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null, null, null, null, null, null);

	public static MatchFilter forTournamentEvent(int tournamentEventId) {
		return new MatchFilter(null, null, null, null, tournamentEventId, null, null, null, null, null, null, null);
	}

	public static MatchFilter forOpponent(int opponentId) {
		return new MatchFilter(null, null, null, null, null, null, null, OpponentFilter.forStats(opponentId), null, null, null, null);
	}

	public static MatchFilter forOpponent(int opponentId, String level, String surface, String round) {
		return new MatchFilter(null, level, surface, null, null, null, round, OpponentFilter.forStats(opponentId), null, null, null, null);
	}

	public static MatchFilter forOpponent(int opponentId, Integer season, String level, String surface, Integer tournamentId, String round, String outcome, String score) {
		return new MatchFilter(season, level, surface, tournamentId, null, null, round, OpponentFilter.forStats(opponentId), OutcomeFilter.forStats(outcome), ScoreFilter.forStats(score), null, null);
	}

	public static MatchFilter forSeason(int season) {
		return new MatchFilter(season, null, null, null, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSurface(String surface) {
		return new MatchFilter(null, null, surface, null, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSeasonAndSurface(int season, String surface) {
		return new MatchFilter(season, null, surface, null, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forMatches(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, String searchPhrase) {
		return new MatchFilter(season, level, surface, tournamentId, tournamentEventId, null, round, opponentFilter, outcomeFilter, scoreFilter, statsFilter, searchPhrase);
	}

	public static MatchFilter forStats(Integer season, String level, String surface) {
		return new MatchFilter(season, level, surface, null, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, Integer opponentId) {
		return new MatchFilter(season, level, surface, tournamentId, null, null, null, OpponentFilter.forStats(opponentId), null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, String round, Integer opponentId) {
		return new MatchFilter(season, level, surface, null, null, null, round, OpponentFilter.forStats(opponentId), null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, String result, StatsFilter statsFilter, String searchPhrase) {
		return forStats(season, level, surface, tournamentId, null, result, null, null, null, null, statsFilter, searchPhrase);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, String searchPhrase) {
		return forStats(season, level, surface, tournamentId, tournamentEventId, null, round, opponentFilter, outcomeFilter, scoreFilter, statsFilter, searchPhrase);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String result, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, String searchPhrase) {
		return new MatchFilter(season, level, surface, tournamentId, tournamentEventId, result, round, opponentFilter, outcomeFilter, scoreFilter, statsFilter, searchPhrase) {
			@Override protected String getSearchCriterion() {
				return STATS_SEARCH_CRITERION;
			}
		};
	}


	// Instance

	private final String round;
	private final OpponentFilter opponentFilter;
	private final OutcomeFilter outcomeFilter;
	private final ScoreFilter scoreFilter;

	private static final String SURFACE_CRITERION        = " AND m.surface = :surface::surface";
	private static final String SURFACES_CRITERION       = " AND m.surface::TEXT IN (:surfaces)";
	private static final String ROUND_CRITERION          = " AND m.round %1$s :round::match_round";
	private static final String MATCHES_SEARCH_CRITERION = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR pw.name ILIKE '%' || :searchPhrase || '%' OR pl.name ILIKE '%' || :searchPhrase || '%')";
	private static final String STATS_SEARCH_CRITERION   = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR o.name ILIKE '%' || :searchPhrase || '%')";

	private MatchFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String result, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, String searchPhrase) {
		super(season, level, surface, tournamentId, tournamentEventId, result, statsFilter, searchPhrase);
		this.round = round;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
		this.outcomeFilter = outcomeFilter != null ? outcomeFilter : OutcomeFilter.ALL;
		this.scoreFilter = scoreFilter != null ? scoreFilter : ScoreFilter.ALL;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(round))
			criteria.append(format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		opponentFilter.appendCriteria(criteria);
		outcomeFilter.appendCriteria(criteria);
		scoreFilter.appendCriteria(criteria);
	}

	@Override public void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (!isNullOrEmpty(round))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
		opponentFilter.addParams(params);
		outcomeFilter.addParams(params);
		scoreFilter.addParams(params);
	}

	@Override protected String getSurfaceCriterion() {
		return SURFACE_CRITERION;
	}

	@Override protected String getSurfacesCriterion() {
		return SURFACES_CRITERION;
	}

	@Override protected String getSearchCriterion() {
		return MATCHES_SEARCH_CRITERION;
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(round) && opponentFilter.isEmpty() && outcomeFilter.isEmpty() && scoreFilter.isEmpty();
	}

	public boolean isTournamentEventFilterEmpty() {
		return super.isEmpty();
	}

	public OpponentFilter getOpponentFilter() {
		return opponentFilter;
	}

	public boolean isForSeason() {
		Integer season = getSeason();
		return season != null && equals(forSeason(season));
	}

	public boolean isForSurface() {
		String surface = getSurface();
		return !isNullOrEmpty(surface) && surface.length() == 1 && equals(forSurface(surface));
	}

	public boolean isForSeasonAndSurface() {
		Integer season = getSeason();
		String surface = getSurface();
		return season != null && !isNullOrEmpty(surface) && surface.length() == 1 && equals(forSeasonAndSurface(season, surface));
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchFilter)) return false;
		if (!super.equals(o)) return false;
		MatchFilter filter = (MatchFilter)o;
		return stringsEqual(round, filter.round) && opponentFilter.equals(filter.opponentFilter) && outcomeFilter.equals(filter.outcomeFilter) && scoreFilter.equals(filter.scoreFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(round), opponentFilter, outcomeFilter, scoreFilter);
	}

	@Override protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("round", emptyToNull(round))
			.add("opponentFilter", opponentFilter)
			.add("outcomeFilter", outcomeFilter)
			.add("scoreFilter", scoreFilter);
	}
}
