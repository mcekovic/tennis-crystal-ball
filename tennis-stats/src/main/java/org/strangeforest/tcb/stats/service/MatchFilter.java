package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class MatchFilter extends TournamentEventResultFilter {

	// Factory

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null, null, null, null);

	public static MatchFilter forTournamentEvent(int tournamentEventId) {
		return new MatchFilter(null, null, null, null, tournamentEventId, null, null, null, null, null);
	}

	public static MatchFilter forOpponent(int opponentId) {
		return new MatchFilter(null, null, null, null, null, null, null, OpponentFilter.forStats(null, opponentId), null, null);
	}

	public static MatchFilter forOpponent(int opponentId, String level, String surface, String round) {
		return new MatchFilter(null, level, surface, null, null, null, round, OpponentFilter.forStats(null, opponentId), null, null);
	}

	public static MatchFilter forOpponent(int opponentId, Integer season, String level, String surface, Integer tournamentId, String round) {
		return new MatchFilter(season, level, surface, tournamentId, null, null, round, OpponentFilter.forStats(null, opponentId), null, null);
	}

	public static MatchFilter forSeason(int season) {
		return new MatchFilter(season, null, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSurface(String surface) {
		return new MatchFilter(null, null, surface, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSeasonAndSurface(int season, String surface) {
		return new MatchFilter(season, null, surface, null, null, null, null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface) {
		return new MatchFilter(season, level, surface, null, null, null, null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, String round, Integer opponentId) {
		return new MatchFilter(season, level, surface, null, null, null, round, OpponentFilter.forStats(opponentId), null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String result, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, String searchPhrase) {
		return new MatchFilter(season, level, surface, tournamentId, tournamentEventId, result, round, opponentFilter, outcomeFilter, searchPhrase) {
			@Override protected String getSearchCriterion() {
				return STATS_SEARCH_CRITERION;
			}
		};
	}


	// Instance

	private final String round;
	private final OpponentFilter opponentFilter;
	private final OutcomeFilter outcomeFilter;

	private static final String SURFACE_CRITERION        = " AND m.surface = :surface::surface";
	private static final String ROUND_CRITERION          = " AND m.round %1$s :round::match_round";
	private static final String MATCHES_SEARCH_CRITERION = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR pw.name ILIKE '%' || :searchPhrase || '%' OR pl.name ILIKE '%' || :searchPhrase || '%')";
	private static final String STATS_SEARCH_CRITERION   = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR o.name ILIKE '%' || :searchPhrase || '%')";

	public MatchFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String result, String round, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, String searchPhrase) {
		super(season, level, surface, tournamentId, tournamentEventId, result, searchPhrase);
		this.round = round;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
		this.outcomeFilter = outcomeFilter != null ? outcomeFilter : OutcomeFilter.ALL;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(round))
			criteria.append(format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		opponentFilter.appendCriteria(criteria);
		outcomeFilter.appendCriteria(criteria);
	}

	@Override public void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (!isNullOrEmpty(round))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
		opponentFilter.addParams(params);
		outcomeFilter.addParams(params);
	}

	@Override protected String getSurfaceCriterion() {
		return SURFACE_CRITERION;
	}

	@Override protected String getSearchCriterion() {
		return MATCHES_SEARCH_CRITERION;
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(round) && opponentFilter.isEmpty() && outcomeFilter.isEmpty();
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
		return !isNullOrEmpty(surface) && equals(forSurface(surface));
	}

	public boolean isForSeasonAndSurface() {
		Integer season = getSeason();
		String surface = getSurface();
		return season != null && !isNullOrEmpty(surface) && equals(forSeasonAndSurface(season, surface));
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchFilter)) return false;
		if (!super.equals(o)) return false;
		MatchFilter filter = (MatchFilter)o;
		return stringsEqual(round, filter.round) && opponentFilter.equals(filter.opponentFilter) && outcomeFilter.equals(filter.outcomeFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(round), opponentFilter, outcomeFilter);
	}

	@Override protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("round", round)
			.add("opponentFilter", opponentFilter)
			.add("outcomeFilter", outcomeFilter);
	}
}
