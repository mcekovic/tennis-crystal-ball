package org.strangeforest.tcb.stats.util;

import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import org.postgresql.util.*;

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

	public static Boolean getBoolean(ResultSet rs, String column) throws SQLException {
		boolean b = rs.getBoolean(column);
		return rs.wasNull() ? null : b;
	}

	public static String getInternedString(ResultSet rs, String column) throws SQLException {
		String s = rs.getString(column);
		return s != null ? s.intern() : null;
	}

	public static LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
		LocalDate date = rs.getObject(column, LocalDate.class);
		return rs.wasNull() ? null : date;
	}

	private static final double MONTH_FACTOR = 1.0 / 12.0;
	private static final double DAY_FACTOR = 1.0 / 365.25;

	public static Double getYears(ResultSet rs, String column) throws SQLException {
		PGInterval i = rs.getObject(column, PGInterval.class);
		return rs.wasNull() ? null : i.getYears() + MONTH_FACTOR * i.getMonths() + DAY_FACTOR * i.getDays();
	}


	// Binding

	public static void bindStringArray(PreparedStatement ps, int index, String[] strings) throws SQLException {
		ps.setArray(index, ps.getConnection().createArrayOf("text", strings));
	}


	// JSON Parsing

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public static LocalDate parseJSONDate(String date) {
		return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
	}
}
