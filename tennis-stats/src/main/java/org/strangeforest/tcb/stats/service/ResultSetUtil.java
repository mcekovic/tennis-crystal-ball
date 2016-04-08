package org.strangeforest.tcb.stats.service;

import java.sql.*;

public abstract class ResultSetUtil {

	public static Integer getInteger(ResultSet rs, String column) throws SQLException {
		int i = rs.getInt(column);
		return rs.wasNull() ? null : i;
	}
}
