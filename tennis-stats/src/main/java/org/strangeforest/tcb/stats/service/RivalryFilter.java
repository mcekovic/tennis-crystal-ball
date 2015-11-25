package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

public class RivalryFilter {

	private final Range<LocalDate> dateRange;
	private final String level;
	private final String surface;

	private static final String DATE_FROM_CRITERION = " AND date >= ?";
	private static final String DATE_TO_CRITERION   = " AND date <= ?";
	private static final String LEVEL_CRITERION     = " AND level = ?::tournament_level";
	private static final String SURFACE_CRITERION   = " AND surface = ?::surface";

	public RivalryFilter(Range<LocalDate> dateRange, String level, String surface) {
		this.dateRange = dateRange;
		this.level = level;
		this.surface = surface;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean hasLevel() {
		return !isNullOrEmpty(level);
	}

	public boolean hasSurface() {
		return !isNullOrEmpty(surface);
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		appendCriteria(criteria);
		return criteria.toString();
	}

	protected void appendCriteria(StringBuilder criteria) {
		if (dateRange.hasLowerBound())
			criteria.append(DATE_FROM_CRITERION);
		if (dateRange.hasUpperBound())
			criteria.append(DATE_TO_CRITERION);
		if (!isNullOrEmpty(level))
			criteria.append(LEVEL_CRITERION);
		if (!isNullOrEmpty(surface))
			criteria.append(SURFACE_CRITERION);
	}

	public int bindParams(PreparedStatement ps, int index) throws SQLException {
		index = bindDateRange(ps, index, dateRange);
		if (!isNullOrEmpty(level))
			ps.setString(++index, level);
		if (!isNullOrEmpty(surface))
			ps.setString(++index, surface);
		return index;
	}
}