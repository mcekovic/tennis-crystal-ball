package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class TournamentEventFilter {

	private final Integer season;
	private final String level;
	private final String surface;
	private final Integer tournamentId;
	private final String result;
	private final String searchPhrase;

	private static final String SEASON_CRITERION = " AND e.season = ?";
	private static final String LEVEL_CRITERION = " AND e.level = ?::tournament_level";
	private static final String SURFACE_CRITERION = " AND e.surface = ?::surface";
	private static final String TOURNAMENT_CRITERION = " AND e.tournament_id = ?";
	private static final String RESULT_CRITERION = " AND r.result = ?::tournament_event_result";
	private static final String SEARCH_CRITERION = " AND e.name ILIKE '%' || ? || '%'";

	public TournamentEventFilter(Integer season, String level, String surface, Integer tournamentId, String result, String searchPhrase) {
		this.season = season;
		this.level = level;
		this.surface = surface;
		this.tournamentId = tournamentId;
		this.result = result;
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (!isNullOrEmpty(level))
			criteria.append(LEVEL_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		if (!isNullOrEmpty(result))
			criteria.append(RESULT_CRITERION);
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
		return criteria.toString();
	}

	public List<Object> getParamList() {
		List<Object> params = new ArrayList<>();
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(level))
			params.add(level);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		if (!isNullOrEmpty(result))
			params.add(result);
		if (tournamentId != null)
			params.add(tournamentId);
		if (!isNullOrEmpty(searchPhrase))
			params.add(searchPhrase);
		return params;
	}
}
