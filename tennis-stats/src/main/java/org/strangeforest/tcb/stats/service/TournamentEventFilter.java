package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class TournamentEventFilter {

	private final Integer season;
	private final String level;
	private final String surface;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final String searchPhrase;

	private static final String SEASON_CRITERION           = " AND e.season = :season";
	private static final String LEVEL_CRITERION            = " AND e.level = :level::tournament_level";
	private static final String SURFACE_CRITERION          = " AND e.surface = :surface::surface";
	private static final String TOURNAMENT_CRITERION       = " AND e.tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND e.tournament_event_id = :tournamentEventId";
	private static final String SEARCH_CRITERION           = " AND e.name ILIKE '%' || :searchPhrase || '%'";

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
			criteria.append(getSearchCriterion());
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		addParams(params);
		return params;
	}

	protected void addParams(MapSqlParameterSource params) {
		if (season != null)
			params.addValue("season", season);
		if (!isNullOrEmpty(level))
			params.addValue("level", level);
		if (!isNullOrEmpty(surface))
			params.addValue("surface", surface);
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
		if (tournamentEventId != null)
			params.addValue("tournamentEventId", tournamentEventId);
		if (!isNullOrEmpty(searchPhrase))
			params.addValue("searchPhrase", searchPhrase);
	}

	protected String getSearchCriterion() {
		return SEARCH_CRITERION;
	}

	public Integer getSeason() {
		return season;
	}

	public String getSurface() {
		return surface;
	}

	public boolean hasSearchPhrase() {
		return !isNullOrEmpty(searchPhrase);
	}

	public boolean isEmpty() {
		return season == null && isNullOrEmpty(level) && isNullOrEmpty(surface) && tournamentId == null && tournamentEventId == null && isNullOrEmpty(searchPhrase);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TournamentEventFilter)) return false;
		TournamentEventFilter filter = (TournamentEventFilter)o;
		return Objects.equals(season, filter.season) &&
			stringsEqual(level, filter.level) &&
			stringsEqual(surface, filter.surface) &&
			Objects.equals(tournamentId, filter.tournamentId) &&
			Objects.equals(tournamentEventId, filter.tournamentEventId) &&
			stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(season, emptyToNull(level), emptyToNull(surface), tournamentId, tournamentEventId, emptyToNull(searchPhrase));
	}

	@Override public final String toString() {
		return toStringHelper().toString();
	}

	protected ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("season", season)
			.add("level", level)
			.add("surface", surface)
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("searchPhrase", searchPhrase);
	}
}