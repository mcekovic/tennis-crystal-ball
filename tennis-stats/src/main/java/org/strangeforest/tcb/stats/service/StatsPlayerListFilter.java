package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.util.Arrays.asList;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class StatsPlayerListFilter extends PlayerListFilter {

	private final Integer season;
	private final String surface;
	private final Integer tournamentId;
	private final Integer tournamentEventId;

	private static final String SEASON_CRITERION = " AND season = ?";
	private static final String SURFACE_CRITERION = " AND surface = ?::surface";
	private static final String TOURNAMENT_CRITERION = " AND tournament_id = ?";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND tournament_event_id = ?";

	public StatsPlayerListFilter(Integer season) {
		this(null, season);
	}

	public StatsPlayerListFilter(String searchPhrase, Integer season) {
		this(searchPhrase, season, null, null, null);
	}

	public StatsPlayerListFilter(Integer season, String surface, Integer tournamentId, Integer tournamentEventId) {
		this(null, season, surface, tournamentId, tournamentEventId);
	}

	public StatsPlayerListFilter(String searchPhrase, Integer season, String surface, Integer tournamentId, Integer tournamentEventId) {
		super(searchPhrase);
		this.season = season;
		this.surface = surface;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasTournament() {
		return tournamentId != null;
	}

	public boolean hasTournamentEvent() {
		return tournamentEventId != null;
	}

	public boolean hasTournamentOrTournamentEvent() {
		return hasTournament() || hasTournamentEvent();
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getBaseCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendBaseCriteria(criteria);
		return criteria.toString();
	}

	public String getSearchCriteria() {
		StringBuilder criteria = new StringBuilder();
		super.appendCriteria(criteria);
		return criteria.toString();
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		appendBaseCriteria(criteria);
		super.appendCriteria(criteria);
	}

	private void appendBaseCriteria(StringBuilder criteria) {
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (tournamentEventId != null)
			criteria.append(TOURNAMENT_EVENT_CRITERION);
	}

	public Object[] getBaseParams(Object... extraParams) {
		List<Object> params = new ArrayList<>();
		addBaseParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	public Object[] getBaseParamsWithPrefix(Object firstParam, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		params.add(firstParam);
		addBaseParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	public Object[] getParams(Collection midParams, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		addBaseParams(params);
		params.addAll(midParams);
		super.addParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	public Object[] getParamsWithPrefix(Object firstParam, Collection midParams, Object... extraParams) {
		List<Object> params = new ArrayList<>();
		params.add(firstParam);
		addBaseParams(params);
		params.addAll(midParams);
		super.addParams(params);
		params.addAll(asList(extraParams));
		return params.toArray();
	}

	@Override protected void addParams(List<Object> params) {
		addBaseParams(params);
		super.addParams(params);
	}

	private void addBaseParams(List<Object> params) {
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		if (tournamentId != null)
			params.add(tournamentId);
		if (tournamentEventId != null)
			params.add(tournamentEventId);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StatsPlayerListFilter)) return false;
		if (!super.equals(o)) return false;
		StatsPlayerListFilter filter = (StatsPlayerListFilter)o;
		return Objects.equals(season, filter.season) &&	stringsEqual(surface, filter.surface)
		    && Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, surface, tournamentId, tournamentEventId);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("season", season)
			.add("surface", surface)
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.toString();
	}
}
