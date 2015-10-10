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
}
