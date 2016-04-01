package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

abstract class FilterUtil {

	static boolean stringsEqual(String s1, String s2) {
		return Objects.equals(emptyToNull(s1), emptyToNull(s2));
	}

	private static final String AND = " AND";

	static String where(String condition) {
		return !isNullOrEmpty(condition) ? "\nWHERE" + skipAnd(condition) : "";
	}

	static String where(String condition, int indent) {
		return !isNullOrEmpty(condition) ? "\n" + repeat(" ", indent) + "WHERE" + skipAnd(condition) : "";
	}

	private static String skipAnd(String condition) {
		return condition.startsWith(AND) ? condition.substring(AND.length()) : condition;
	}
}
