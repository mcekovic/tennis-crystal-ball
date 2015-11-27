package org.strangeforest.tcb.stats.util;

import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.util.*;

import com.google.common.collect.*;

public abstract class ResultSetUtil {

	public static Integer getInteger(ResultSet rs, String column) throws SQLException {
		int i = rs.getInt(column);
		return rs.wasNull() ? null : i;
	}

	public static int bindIntegerRange(PreparedStatement ps, int index, Range<Integer> integerRange) throws SQLException {
		if (integerRange.hasLowerBound())
			ps.setInt(++index, integerRange.lowerEndpoint());
		if (integerRange.hasUpperBound())
			ps.setInt(++index, integerRange.upperEndpoint());
		return index;
	}

	public static int bindDateRange(PreparedStatement ps, int index, Range<LocalDate> dateRange) throws SQLException {
		if (dateRange.hasLowerBound())
			ps.setDate(++index, Date.valueOf(dateRange.lowerEndpoint()));
		if (dateRange.hasUpperBound())
			ps.setDate(++index, Date.valueOf(dateRange.upperEndpoint()));
		return index;
	}

	public static void bindIntegerArray(PreparedStatement ps, int index, List<Integer> arr) throws SQLException {
		ps.setArray(index, ps.getConnection().createArrayOf("integer", arr.toArray()));
	}
}
