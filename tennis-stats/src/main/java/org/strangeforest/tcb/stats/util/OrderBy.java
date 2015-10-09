package org.strangeforest.tcb.stats.util;

public class OrderBy {

	public static OrderBy asc(String column) {
		return new OrderBy(column, false);
	}

	public static OrderBy desc(String column) {
		return new OrderBy(column, true);
	}

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
		return desc ? column + " DESC" : column;
	}
}
