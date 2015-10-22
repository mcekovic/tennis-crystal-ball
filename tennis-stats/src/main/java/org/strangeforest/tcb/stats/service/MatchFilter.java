package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class MatchFilter extends TournamentEventFilter {

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null);

	private final String round;

	private static final String ROUND_CRITERION = " AND m.round = ?::match_round";

	public MatchFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String round, String searchPhrase) {
		super(season, level, surface, tournamentId, tournamentEventId, searchPhrase);
		this.round = round;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(round))
			criteria.append(ROUND_CRITERION);
	}

	@Override public List<Object> getParamList() {
		List<Object> params = super.getParamList();
		if (!isNullOrEmpty(round))
			params.add(round);
		return params;
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(round);
	}

	public boolean isTournamentEventFilterEmpty() {
		return super.isEmpty();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchFilter)) return false;
		if (!super.equals(o)) return false;
		MatchFilter filter = (MatchFilter)o;
		return stringsEqual(round, filter.round);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(round));
	}
}
