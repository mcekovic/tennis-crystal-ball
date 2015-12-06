package org.strangeforest.tcb.stats.model;

public final class PerformanceCategory {

	private final String name;
	private final String column;
	private final int minEntries;
	private final String entriesName;

	public PerformanceCategory(String name, String column, int minEntries, String entriesName) {
		this.name = name;
		this.column = column;
		this.minEntries = minEntries;
		this.entriesName = entriesName;
	}

	public String getName() {
		return name;
	}

	public String getColumn() {
		return column;
	}

	public int getMinEntries() {
		return minEntries;
	}

	public String getEntriesName() {
		return entriesName;
	}
}
