package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public abstract class ResultSetUtil {

	// Extracting

	public static Integer getInteger(ResultSet rs, String column) throws SQLException {
		int i = rs.getInt(column);
		return rs.wasNull() ? null : i;
	}

	public static List<Integer> getIntegers(ResultSet rs, String column) throws SQLException {
		Array sqlArray = rs.getArray(column);
		if (sqlArray == null)
			return emptyList();
		Object array = sqlArray.getArray();
		if (array instanceof Integer[])
			return asList((Integer[])array);
		else if (array instanceof Number[])
			return Stream.of((Number[])array).map(n -> n != null ? n.intValue() : null).collect(toList());
		else
			throw new IllegalArgumentException("Incompatible type with Integer[]: " + array.getClass());
	}

	public static Double getDouble(ResultSet rs, String column) throws SQLException {
		double d = rs.getDouble(column);
		return rs.wasNull() ? null : d;
	}

	public static LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
		LocalDate date = rs.getObject(column, LocalDate.class);
		return rs.wasNull() ? null : date;
	}


	// Binding

	public static void bindStringArray(PreparedStatement ps, int index, String[] strings) throws SQLException {
		ps.setArray(index, ps.getConnection().createArrayOf("text", strings));
	}
}
