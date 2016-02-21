package org.strangeforest.tcb.stats.util;

public class OrderBy {

	// Factory

	public static OrderBy asc(String column) {
		return new OrderBy(column, false);
	}

	public static OrderBy desc(String column) {
		return new OrderBy(column, true);
	}

	private static final String DESC = "DESC";
	private static final String NULLS_LAST = " NULLS LAST";


	// Instance

	private final String column;
	private final boolean desc;

	private OrderBy(String column, boolean desc) {
		this.column = column;
		this.desc = desc;
	}

	public String getColumn() {
		return column;
	}

	public boolean isDesc() {
		return desc;
	}

	@Override public String toString() {
		return desc ? addSort(column, DESC) : column;
	}

	public static String addSort(String column, String sort) {
		if (column.endsWith(NULLS_LAST))
			return column.substring(0, column.length() - NULLS_LAST.length()) + " " + sort + NULLS_LAST;
		else
			return column + " " + sort;
	}
}
