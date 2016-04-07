package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class TournamentEventResultFilter extends TournamentEventFilter {

	private final String result;

	private static final String RESULT_CRITERION = " AND r.result %1$s :result::tournament_event_result";

	public TournamentEventResultFilter(Integer season, String level, String surface, Integer tournamentId, String result, String searchPhrase) {
		super(season, level, surface, tournamentId, null, searchPhrase);
		this.result = result;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (!isNullOrEmpty(result))
			criteria.append(format(RESULT_CRITERION, result.endsWith("+") ? ">=" : "="));
	}

	@Override protected void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (!isNullOrEmpty(result))
			params.addValue("result", result.endsWith("+") ? result.substring(0, result.length() - 1) : result);
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && isNullOrEmpty(result);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TournamentEventResultFilter)) return false;
		if (!super.equals(o)) return false;
		TournamentEventResultFilter filter = (TournamentEventResultFilter)o;
		return stringsEqual(result, filter.result);
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), emptyToNull(result));
	}

	@Override protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("result", result);
	}
}
