package org.strangeforest.tcb.stats.util;

import java.sql.*;

public class ResultSetUtil {

	public static Integer getInteger(ResultSet rs, String column) throws SQLException {
		int i = rs.getInt(column);
		return rs.wasNull() ? null : i;
	}
}
