package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.MoreObjects.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class TournamentEventResultFilter extends TournamentEventFilter {

	public static final TournamentEventResultFilter EMPTY = new TournamentEventResultFilter(null, null, null, null, null, null, null, null, null, null, null);

	private final String result;
	private final StatsFilter statsFilter;

	private static final String RESULT_CRITERION = " AND r.result %1$s :result::tournament_event_result";

	public TournamentEventResultFilter(Integer season) {
		this(season, null, null, null, null, null, null, null, null, null, null);
	}

	public TournamentEventResultFilter(Integer season, Range<LocalDate> dateRange, String level, String surface, Boolean indoor, Range<Integer> speedRange, String result, Integer tournamentId, StatsFilter statsFilter, String searchPhrase) {
		this(season, dateRange, level, surface, indoor, speedRange, result, tournamentId, null, statsFilter, searchPhrase);
	}

	protected TournamentEventResultFilter(Integer season, Range<LocalDate> dateRange, String level, String surface, Boolean indoor, Range<Integer> speedRange, String result, Integer tournamentId, Integer tournamentEventId, StatsFilter statsFilter, String searchPhrase) {
		super(season, dateRange, level, surface, indoor, speedRange, tournamentId, tournamentEventId, searchPhrase);
		this.result = result;
		this.statsFilter = statsFilter != null ? statsFilter : StatsFilter.ALL;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(result))
			criteria.append(format(RESULT_CRITERION, result.endsWith("+") ? ">=" : "="));
		statsFilter.appendCriteria(criteria);
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (!isNullOrEmpty(result))
			params.addValue("result", result.endsWith("+") ? result.substring(0, result.length() - 1) : result);
		statsFilter.addParams(params);
	}

	public boolean hasResult() {
		return !isNullOrEmpty(result);
	}

	public boolean hasStatsFilter() {
		return !statsFilter.isEmpty();
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(result) && statsFilter.isEmpty();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TournamentEventResultFilter)) return false;
		if (!super.equals(o)) return false;
		TournamentEventResultFilter filter = (TournamentEventResultFilter)o;
		return stringsEqual(result, filter.result) && statsFilter.equals(filter.statsFilter);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(result), statsFilter);
	}

	@Override protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("result", emptyToNull(result))
			.add("statsFilter", statsFilter);
	}
}
