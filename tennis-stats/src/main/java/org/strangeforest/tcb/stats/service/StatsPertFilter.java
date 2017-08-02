package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class StatsPertFilter extends PlayerListFilter {

	// Factory

	public static final StatsPertFilter EMPTY = new StatsPertFilter(null, null, null, null, null, null, null, null);

 	public static StatsPertFilter forSeason(Integer season) {
		return new StatsPertFilter(null, null, season, null, null, null, null, null);
	}

	public static StatsPertFilter forSeasonAndTournament(Integer season, Integer tournamentId) {
		return new StatsPertFilter(null, null, season, null, null, tournamentId, null, null);
	}

	public static StatsPertFilter forSeasonAndTournament(Boolean active, String searchPhrase, Integer season, Integer tournamentId) {
		return new StatsPertFilter(active, searchPhrase, season, null, null, tournamentId, null, null);
	}


	// Instance

	private final Integer season;
	private final String level;
	private final String surface;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final Integer opponentId;

	private static final String SEASON_CRITERION           = " AND season = :season";
	private static final String LEVEL_CRITERION            = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION           = " AND level::TEXT IN (:levels)";
	private static final String SURFACE_CRITERION          = " AND surface = :surface::surface";
	private static final String SURFACES_CRITERION         = " AND surface::TEXT IN (:surfaces)";
	private static final String TOURNAMENT_CRITERION       = " AND tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND tournament_event_id = :tournamentEventId";
	private static final String OPPONENT_CRITERION         = " AND opponent_id = :opponentId";

	public StatsPertFilter(Integer season, String surface, Integer tournamentId, Integer tournamentEventId) {
		this(null, null, season, null, surface, tournamentId, tournamentEventId, null);
	}

	public StatsPertFilter(Boolean active, String searchPhrase, Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, Integer opponentId) {
		super(active, searchPhrase);
		this.season = season;
		this.level = level;
		this.surface = surface;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.opponentId = opponentId;
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
		if (!isNullOrEmpty(level))
			criteria.append(level.length() == 1 ? LEVEL_CRITERION : LEVELS_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(surface.length() == 1 ? SURFACE_CRITERION : SURFACES_CRITERION);
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (tournamentEventId != null)
			criteria.append(TOURNAMENT_EVENT_CRITERION);
		if (opponentId != null)
			criteria.append(OPPONENT_CRITERION);
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (season != null)
			params.addValue("season", season);
		if (!isNullOrEmpty(surface)) {
			if (surface.length() == 1)
				params.addValue("surface", surface);
			else
				params.addValue("surfaces", asList(surface.split("")));
		}
		if (!isNullOrEmpty(level)) {
			if (level.length() == 1)
				params.addValue("level", level);
			else
				params.addValue("levels", asList(level.split("")));
		}
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
		if (tournamentEventId != null)
			params.addValue("tournamentEventId", tournamentEventId);
		if (opponentId != null)
			params.addValue("opponentId", opponentId);
	}

	public Integer getSeason() {
		return season;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasSurfaceGroup() {
		return hasSurface() && surface.length() > 1;
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

	public boolean hasOpponent() {
		return opponentId != null;
	}

	public boolean isForSeason() {
		return season != null && equals(forSeason(season));
	}

	public boolean isEmpty() {
		return season == null && isNullOrEmpty(level) && isNullOrEmpty(surface) && tournamentId == null && tournamentEventId == null && opponentId == null;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StatsPertFilter)) return false;
		if (!super.equals(o)) return false;
		StatsPertFilter filter = (StatsPertFilter)o;
		return Objects.equals(season, filter.season) &&	stringsEqual(level, filter.level) &&	stringsEqual(surface, filter.surface)
		    && Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId) && Objects.equals(opponentId, filter.opponentId);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, emptyToNull(level), emptyToNull(surface), tournamentId, tournamentEventId, opponentId);
	}

	@Override protected MoreObjects.ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("season", season)
			.add("surface", surface)
			.add("level", level)
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("opponentId", opponentId);
	}
}
