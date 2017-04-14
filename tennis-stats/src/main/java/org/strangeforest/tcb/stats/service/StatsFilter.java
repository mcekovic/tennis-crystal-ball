package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

public class StatsFilter {

	// Factory

	public static final StatsFilter ALL = new StatsFilter(null, null);


	// Instance

	private final String category;
	private final Range<Double> range;

	public StatsFilter(String category, Double from, Double to) {
		this(category, RangeUtil.toRange(from, to));
	}

	private StatsFilter(String category, Range<Double> range) {
		this.category = category;
		this.range = range;
	}

	void appendCriteria(StringBuilder criteria) {
		if (!isNullOrEmpty(category)) {
			String expression = StatsCategory.get(category).getExpression();
			appendRangeFilter(criteria, range, '(' + expression + ')', "stats");
		}
	}

	void addParams(MapSqlParameterSource params) {
		if (!isNullOrEmpty(category))
			addRangeParams(params, range, "stats");
	}

	public boolean isEmpty() {
		return isNullOrEmpty(category);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StatsFilter)) return false;
		StatsFilter filter = (StatsFilter)o;
		return stringsEqual(category, filter.category) && Objects.equals(range, filter.range);
	}

	@Override public int hashCode() {
		return Objects.hash(emptyToNull(category), range);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("category", category)
			.add("range", range)
			.toString();
	}}
