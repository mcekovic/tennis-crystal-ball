package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class InProgressEventFilter {

	public static final InProgressEventFilter ALL_IN_PROGRESS = new InProgressEventFilter(false, null);

	private final boolean completed;
	private final String searchPhrase;

	private static final String NOT_COMPLETED_CRITERION = "AND NOT completed AND NOT exists(SELECT te.tournament_event_id FROM tournament_event te WHERE te.tournament_id = e.tournament_id AND te.season = extract(YEAR FROM tournament_end(e.date, e.level, e.draw_size)))";
	private static final String SEARCH_CRITERION = " AND name ILIKE '%' || :searchPhrase || '%'";

	public InProgressEventFilter(boolean completed, String searchPhrase) {
		this.completed = completed;
		this.searchPhrase = searchPhrase;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (!completed)
			criteria.append(NOT_COMPLETED_CRITERION);
		if (!isNullOrEmpty(searchPhrase))
			criteria.append(SEARCH_CRITERION);
		return criteria.toString();
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		if (!isNullOrEmpty(searchPhrase))
			params.addValue("searchPhrase", searchPhrase);
		return params;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof InProgressEventFilter)) return false;
		InProgressEventFilter filter = (InProgressEventFilter)o;
		return completed == filter.completed && stringsEqual(searchPhrase, filter.searchPhrase);
	}

	@Override public int hashCode() {
		return Objects.hash(completed, emptyToNull(searchPhrase));
	}

	@Override public final String toString() {
		return toStringHelper().toString();
	}

	protected ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("completed", nullIf(completed, false))
			.add("searchPhrase", emptyToNull(searchPhrase));
	}
}