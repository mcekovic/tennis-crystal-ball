package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class MatchFilter extends TournamentEventFilter {

	// Factory

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null, null);

	public static MatchFilter forTournamentEvent(int tournamentEventId) {
		return new MatchFilter(null, null, null, null, tournamentEventId, null, null, null);
	}

	public static MatchFilter forSeason(int season) {
		return new MatchFilter(season, null, null, null, null, null, null, null);
	}

	public static MatchFilter forSurface(String surface) {
		return new MatchFilter(null, null, surface, null, null, null, null, null);
	}

	public static MatchFilter forSeasonAndSurface(int season, String surface) {
		return new MatchFilter(season, null, surface, null, null, null, null, null);
	}


	// Instance

	private final String round;
	private final OpponentFilter opponentFilter;

	private static final String ROUND_CRITERION = " AND m.round = ?::match_round";

	public MatchFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, String searchPhrase, OpponentFilter opponentFilter) {
		super(season, level, surface, tournamentId, tournamentEventId, searchPhrase);
		this.round = round;
		this.opponentFilter = opponentFilter;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(round))
			criteria.append(ROUND_CRITERION);
		if (opponentFilter != null)
			criteria.append(opponentFilter.getCriterion());
	}

	@Override public List<Object> getParamList() {
		List<Object> params = super.getParamList();
		if (!isNullOrEmpty(round))
			params.add(round);
		if (opponentFilter != null && opponentFilter.isForMatches()) {
			int playerId = opponentFilter.getPlayerId();
			params.add(playerId);
			params.add(playerId);
		}
		return params;
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(round) && opponentFilter == null;
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
		return stringsEqual(round, filter.round) && Objects.equals(opponentFilter, filter.opponentFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(round), opponentFilter);
	}
}
