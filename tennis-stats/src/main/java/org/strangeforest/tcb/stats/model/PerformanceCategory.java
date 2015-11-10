package org.strangeforest.tcb.stats.model;

public final class PerformanceCategory {

	private final String name;
	private final int minEntries;
	private final String entriesName;

	public PerformanceCategory(String name, int minEntries, String entriesName) {
		this.name = name;
		this.minEntries = minEntries;
		this.entriesName = entriesName;
	}

	public String getName() {
		return name;
	}

	public int getMinEntries() {
		return minEntries;
	}

	public String getEntriesName() {
		return entriesName;
	}
}
