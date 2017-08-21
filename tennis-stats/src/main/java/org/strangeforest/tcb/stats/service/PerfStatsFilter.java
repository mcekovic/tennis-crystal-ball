package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.format;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class PerfStatsFilter extends PlayerListFilter {

	// Factory

	public static final PerfStatsFilter EMPTY = new PerfStatsFilter(null, null, null, null, null, null, null, null, null);

 	public static PerfStatsFilter forSeason(Integer season) {
		return new PerfStatsFilter(null, null, season, null, null, null, null, null, null);
	}

 	public static PerfStatsFilter forTournament(Integer tournamentId) {
		return new PerfStatsFilter(null, null, null, null, null, null, tournamentId, null, null);
	}


	// Instance

	private final Integer season;
	private final String level;
	private final String surface;
	private final String round;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final OpponentFilter opponentFilter;

	private static final String SEASON_CRITERION           = " AND season = :season";
	private static final String LEVEL_CRITERION            = " AND level = :level::tournament_level";
	private static final String LEVELS_CRITERION           = " AND level::TEXT IN (:levels)";
	private static final String SURFACE_CRITERION          = " AND surface = :surface::surface";
	private static final String SURFACES_CRITERION         = " AND surface::TEXT IN (:surfaces)";
	private static final String ROUND_CRITERION            = " AND round %1$s :round::match_round AND level NOT IN ('D', 'T')";
	private static final String TOURNAMENT_CRITERION       = " AND tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND tournament_event_id = :tournamentEventId";

	public PerfStatsFilter(Integer season, String surface, Integer tournamentId, Integer tournamentEventId) {
		this(null, null, season, null, surface, null, tournamentId, tournamentEventId, null);
	}

	public PerfStatsFilter(Integer season, String level, String surface, Integer tournamentId, Integer tournamentEventId, Integer opponentId) {
		this(null, null, season, level, surface, null, tournamentId, tournamentEventId, OpponentFilter.forStats(opponentId));
	}
	
	public PerfStatsFilter(Boolean active, String searchPhrase, Integer season, String level, String surface, String round, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter) {
		super(active, searchPhrase);
		this.season = season;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
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
		if (!isNullOrEmpty(round))
			criteria.append(format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		if (tournamentId != null)
			criteria.append(TOURNAMENT_CRITERION);
		if (tournamentEventId != null)
			criteria.append(TOURNAMENT_EVENT_CRITERION);
		opponentFilter.appendCriteria(criteria);
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (season != null)
			params.addValue("season", season);
		if (!isNullOrEmpty(level)) {
			if (level.length() == 1)
				params.addValue("level", level);
			else
				params.addValue("levels", asList(level.split("")));
		}
		if (!isNullOrEmpty(surface)) {
			if (surface.length() == 1)
				params.addValue("surface", surface);
			else
				params.addValue("surfaces", asList(surface.split("")));
		}
		if (!isNullOrEmpty(round))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
		if (tournamentEventId != null)
			params.addValue("tournamentEventId", tournamentEventId);
		opponentFilter.addParams(params);
	}

	public Integer getSeason() {
		return season;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getRound() {
		return round;
	}

	public OpponentFilter getOpponentFilter() {
		return opponentFilter;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public boolean hasSurfaceGroup() {
		return hasSurface() && surface.length() > 1;
	}

	public boolean hasRound() {
		return !isNullOrEmpty(round);
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
		return !opponentFilter.isEmpty();
	}

	public boolean isForSeason() {
		return season != null && equals(forSeason(season));
	}

	public boolean isForTournament() {
		return tournamentId != null && equals(forTournament(tournamentId));
	}

	public boolean isEmpty() {
		return season == null && isNullOrEmpty(level) && isNullOrEmpty(surface) && isNullOrEmpty(round) && tournamentId == null && tournamentEventId == null && opponentFilter.isEmpty();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PerfStatsFilter)) return false;
		if (!super.equals(o)) return false;
		PerfStatsFilter filter = (PerfStatsFilter)o;
		return Objects.equals(season, filter.season) &&	stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface) && stringsEqual(round, filter.round)
		    && Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId) && opponentFilter.equals(filter.opponentFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), season, emptyToNull(level), emptyToNull(surface), emptyToNull(round), tournamentId, tournamentEventId, opponentFilter);
	}

	@Override protected MoreObjects.ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("season", season)
			.add("level", level)
			.add("surface", surface)
			.add("round", round)
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("opponentFilter", opponentFilter);
	}
}
