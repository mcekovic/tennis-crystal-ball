package org.strangeforest.tcb.stats.util;

import java.sql.*;
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

	public static void bindIntegerArray(PreparedStatement ps, int index, Collection<Integer> integers) throws SQLException {
		ps.setArray(index, ps.getConnection().createArrayOf("integer", integers.toArray()));
	}
}
