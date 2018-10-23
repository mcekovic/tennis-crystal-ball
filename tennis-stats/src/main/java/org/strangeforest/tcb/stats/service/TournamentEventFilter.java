package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class TournamentEventFilter {

	private final Integer season;
	private final boolean last52Weeks;
	private final Range<LocalDate> dateRange;
	private final String level;
	private final String surface;
	private final Boolean indoor;
	private final Range<Integer> speedRange;
	private final Integer tournamentId;
	private final Integer tournamentEventId;
	private final String searchPhrase;

	private static final String SEASON_CRITERION           = " AND e.season = :season";
	private static final String LAST_52_WEEKS_CRITERION    = " AND %1$sdate >= current_date - INTERVAL '1 year'";
	private static final String LEVEL_CRITERION            = " AND e.level = :level::tournament_level";
	private static final String LEVELS_CRITERION           = " AND e.level::TEXT IN (:levels)";
	private static final String SURFACE_CRITERION          = " AND %1$ssurface = :surface::surface";
	private static final String SURFACES_CRITERION         = " AND %1$ssurface::TEXT IN (:surfaces)";
	private static final String INDOOR_CRITERION           = " AND %1$sindoor = :indoor";
	private static final String TOURNAMENT_CRITERION       = " AND e.tournament_id = :tournamentId";
	private static final String TOURNAMENT_EVENT_CRITERION = " AND e.tournament_event_id = :tournamentEventId";
	private static final String SEARCH_CRITERION           = " AND e.name ILIKE '%' || :searchPhrase || '%'";

	private static final int LAST_52_WEEKS_SEASON = -1;

	public TournamentEventFilter(Integer season, Range<LocalDate> dateRange, String level, String surface, Boolean indoor, Range<Integer> speedRange, Integer tournamentId, Integer tournamentEventId, String searchPhrase) {
		this.season = season != null && season != LAST_52_WEEKS_SEASON ? season : null;
		last52Weeks = season != null && season == LAST_52_WEEKS_SEASON;
		this.dateRange = dateRange != null ? dateRange : Range.all();
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.speedRange = speedRange != null ? speedRange : Range.all();
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
		if (last52Weeks)
			criteria.append(format(LAST_52_WEEKS_CRITERION, getPrefix()));
		appendRangeFilter(criteria, dateRange, getPrefix() + "date", "date");
		if (!isNullOrEmpty(level))
			criteria.append(level.length() == 1 ? LEVEL_CRITERION : LEVELS_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(format(surface.length() == 1 ? SURFACE_CRITERION : SURFACES_CRITERION, getPrefix()));
		if (indoor != null)
			criteria.append(format(INDOOR_CRITERION, getPrefix()));
		appendRangeFilter(criteria, speedRange, "es.court_speed", "speed");
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
		addRangeParams(params, dateRange, "date");
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
		if (indoor != null)
			params.addValue("indoor", indoor);
		addRangeParams(params, speedRange, "speed");
		if (tournamentId != null)
			params.addValue("tournamentId", tournamentId);
		if (tournamentEventId != null)
			params.addValue("tournamentEventId", tournamentEventId);
		if (!isNullOrEmpty(searchPhrase))
			params.addValue("searchPhrase", searchPhrase);
	}

	protected String getPrefix() {
		return "e.";
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

	public boolean hasSpeedRange() {
		return !speedRange.equals(Range.all());
	}

	public boolean hasSearchPhrase() {
		return !isNullOrEmpty(searchPhrase);
	}

	public boolean isEmpty() {
		return season == null && !last52Weeks && dateRange.equals(Range.all()) &&
			isNullOrEmpty(level) && isNullOrEmpty(surface) && indoor == null && speedRange.equals(Range.all()) &&
			tournamentId == null && tournamentEventId == null && isNullOrEmpty(searchPhrase);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TournamentEventFilter)) return false;
		TournamentEventFilter filter = (TournamentEventFilter)o;
		return Objects.equals(season, filter.season) &&	last52Weeks == filter.last52Weeks && dateRange.equals(filter.dateRange) &&
			stringsEqual(level, filter.level) && stringsEqual(surface, filter.surface) && Objects.equals(indoor, filter.indoor) && speedRange.equals(filter.speedRange) &&
			Objects.equals(tournamentId, filter.tournamentId) && Objects.equals(tournamentEventId, filter.tournamentEventId) &&
			stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(season, last52Weeks, dateRange, emptyToNull(level), emptyToNull(surface), indoor, speedRange, tournamentId, tournamentEventId, emptyToNull(searchPhrase));
	}

	@Override public final String toString() {
		return toStringHelper().toString();
	}

	protected ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("season", season)
			.add("last52Weeks", nullIf(last52Weeks, true))
			.add("dateRange", nullIf(dateRange, Range.all()))
			.add("level", emptyToNull(level))
			.add("surface", emptyToNull(surface))
			.add("indoor", indoor)
			.add("speedRange", nullIf(speedRange, Range.all()))
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("searchPhrase", emptyToNull(searchPhrase));
	}
}