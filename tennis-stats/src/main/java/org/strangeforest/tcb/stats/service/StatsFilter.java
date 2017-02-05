package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

import java.lang.String;

public class StatsFilter {

	// Factory

	public static final StatsFilter ALL = new StatsFilter(null, null);


	// Instance

	private final String category;
	private final Range<Double> range;

	private static final String FROM_STAT_CRITERION = " AND (%1$s) >= :fromStat";
	private static final String TO_STAT_CRITERION = " AND (%1$s) <= :toStat";

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
			if (range.hasLowerBound())
				criteria.append(format(FROM_STAT_CRITERION, expression));
			if (range.hasUpperBound())
				criteria.append(format(TO_STAT_CRITERION, expression));
		}
	}

	void addParams(MapSqlParameterSource params) {
		if (range != null) {
			if (range.hasLowerBound())
				params.addValue("fromStat", range.lowerEndpoint());
			if (range.hasUpperBound())
				params.addValue("toStat", range.upperEndpoint());
		}
	}

	public boolean isEmpty() {
		return isNullOrEmpty(category);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StatsFilter)) return false;
		StatsFilter filter = (StatsFilter)o;
		return Objects.equals(category, filter.category) && Objects.equals(range, filter.range);
	}

	@Override public int hashCode() {
		return Objects.hash(category, range);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("category", category)
			.add("range", range)
			.toString();
	}}
