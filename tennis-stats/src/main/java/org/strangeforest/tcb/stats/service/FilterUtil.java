package org.strangeforest.tcb.stats.service;

import java.util.*;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;

abstract class FilterUtil {

	private static final String AND = " AND ";

	static String where(String condition) {
		return where(condition, 0);
	}

	static String where(String condition, int indent) {
		return !isNullOrEmpty(condition) ? "\n" + repeat(" ", indent) + "WHERE " + skipAnd(condition) : "";
	}

	private static String skipAnd(String condition) {
		return condition.startsWith(AND) ? condition.substring(AND.length()) : condition;
	}

	static String or(String... conditions) {
		StringBuilder or = new StringBuilder();
		for (int i = 0, len = conditions.length; i < len; i++) {
			String condition = conditions[i];
			or.append(i == 0 ? " AND ((" : ") OR (");
			or.append(skipAnd(condition));
		}
		or.append("))");
		return or.toString();
	}

	static String rangeFilter(Range<?> range, String column) {
		StringBuilder condition = new StringBuilder();
		appendRangeFilter(condition, range, column);
		return condition.toString();
	}

	static String rangeFilter(Range<?> range, String column, String param) {
		StringBuilder condition = new StringBuilder();
		appendRangeFilter(condition, range, column, param);
		return condition.toString();
	}

	static void appendRangeFilter(StringBuilder sb, Range<?> range, String column) {
		if (range.hasLowerBound())
			sb.append(AND).append(column).append(" >= ").append(range.lowerEndpoint());
		if (range.hasUpperBound())
			sb.append(AND).append(column).append(" <= ").append(range.upperEndpoint());
	}

	static void appendRangeFilter(StringBuilder sb, Range<?> range, String column, String param) {
		if (range.hasLowerBound())
			sb.append(AND).append(column).append(" >= :").append(param).append("From");
		if (range.hasUpperBound())
			sb.append(AND).append(column).append(" <= :").append(param).append("To");
	}

	static boolean stringsEqual(String s1, String s2) {
		return Objects.equals(emptyToNull(s1), emptyToNull(s2));
	}
}
