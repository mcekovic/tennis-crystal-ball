package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class MatchFilter extends TournamentEventFilter {

	// Factory

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null, null, null);

	public static MatchFilter forTournamentEvent(int tournamentEventId) {
		return new MatchFilter(null, null, null, null, tournamentEventId, null, null, null, null);
	}

	public static MatchFilter forOpponent(int opponentId) {
		return new MatchFilter(null, null, null, null, null, null, OpponentFilter.forStats(null, opponentId), null, null);
	}

	public static MatchFilter forSeason(int season) {
		return new MatchFilter(season, null, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSurface(String surface) {
		return new MatchFilter(null, null, surface, null, null, null, null, null, null);
	}

	public static MatchFilter forSeasonAndSurface(int season, String surface) {
		return new MatchFilter(season, null, surface, null, null, null, null, null, null);
	}

	public static MatchFilter forStats(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, OpponentFilter opponentFilter, WonFilter wonFilter, String searchPhrase) {
		return new MatchFilter(season, level, surface, tournamentId, tournamentEventId, round, opponentFilter, wonFilter, searchPhrase) {
			@Override protected String getSearchCriterion() {
				return STATS_SEARCH_CRITERION;
			}
			@Override protected void addSearchParams(List<Object> params) {
				params.add(getSearchPhrase());
				params.add(getSearchPhrase());
			}
		};
	}


	// Instance

	private final String round;
	private final OpponentFilter opponentFilter;
	private final WonFilter wonFilter;

	private static final String ROUND_CRITERION          = " AND m.round = ?::match_round";
	private static final String MATCHES_SEARCH_CRITERION = " AND (e.name ILIKE '%' || ? || '%' OR pw.name ILIKE '%' || ? || '%' OR pl.name ILIKE '%' || ? || '%')";
	private static final String STATS_SEARCH_CRITERION   = " AND (e.name ILIKE '%' || ? || '%' OR o.name ILIKE '%' || ? || '%')";

	public MatchFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, OpponentFilter opponentFilter, WonFilter wonFilter, String searchPhrase) {
		super(season, level, surface, tournamentId, tournamentEventId, searchPhrase);
		this.round = round;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
		this.wonFilter = wonFilter != null ? wonFilter : WonFilter.ALL;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(round))
			criteria.append(ROUND_CRITERION);
		opponentFilter.appendCriteria(criteria);
		wonFilter.appendCriteria(criteria);
	}

	@Override public List<Object> getParamList() {
		List<Object> params = super.getParamList();
		if (!isNullOrEmpty(round))
			params.add(round);
		opponentFilter.addParams(params);
		wonFilter.addParams(params);
		return params;
	}

	@Override protected String getSearchCriterion() {
		return MATCHES_SEARCH_CRITERION;
	}

	@Override protected void addSearchParams(List<Object> params) {
		params.add(getSearchPhrase());
		params.add(getSearchPhrase());
		params.add(getSearchPhrase());
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(round) && opponentFilter.isEmpty() && wonFilter.isEmpty();
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
		return stringsEqual(round, filter.round) && opponentFilter.equals(filter.opponentFilter) && wonFilter.equals(filter.wonFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(round), opponentFilter, wonFilter);
	}
}
