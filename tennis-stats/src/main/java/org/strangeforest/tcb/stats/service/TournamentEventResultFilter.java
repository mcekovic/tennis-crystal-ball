package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class TournamentEventResultFilter extends TournamentEventFilter {

	private final String result;

	private static final String RESULT_CRITERION = " AND r.result = ?::tournament_event_result";

	public TournamentEventResultFilter(Integer season, String level, String surface, Integer tournamentId, String result, String searchPhrase) {
		super(season, level, surface, tournamentId, null, searchPhrase);
		this.result = result;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(result))
			criteria.append(RESULT_CRITERION);
	}

	@Override public List<Object> getParamList() {
		List<Object> params = super.getParamList();
		if (!isNullOrEmpty(result))
			params.add(result);
		return params;
	}
}
