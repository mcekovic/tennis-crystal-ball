package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class TournamentEventFilter {

	private final Integer season;
	private final String level;
	private final String surface;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final String searchPhrase;

	private static final String SEASON_CRITERION           = " AND e.season = ?";
	private static final String LEVEL_CRITERION            = " AND e.level = ?::tournament_level";
	private static final String SURFACE_CRITERION          = " AND e.surface = ?::surface";
	private static final String TOURNAMENT_CRITERION       = " AND e.tournament_id = ?";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND e.tournament_event_id = ?";
	private static final String SEARCH_CRITERION           = " AND e.name ILIKE '%' || ? || '%'";

	public TournamentEventFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, String searchPhrase) {
		this.season = season;
		this.level = level;
		this.surface = surface;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	protected void appendCriteria(StringBuilder criteria) {
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (!isNullOrEmpty(level))
			criteria.append(LEVEL_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (tournamentEventId != null)
			criteria.append(TOURNAMENT_EVENT_CRITERION);
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
	}

	public List<Object> getParamList() {
		List<Object> params = new ArrayList<>();
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(level))
			params.add(level);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		if (tournamentId != null)
			params.add(tournamentId);
		if (tournamentEventId != null)
			params.add(tournamentEventId);
		if (!isNullOrEmpty(searchPhrase))
			params.add(searchPhrase);
		return params;
	}
}
