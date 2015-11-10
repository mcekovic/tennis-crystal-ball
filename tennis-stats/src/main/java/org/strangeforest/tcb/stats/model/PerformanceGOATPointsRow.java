package org.strangeforest.tcb.stats.model;

public class PerformanceGOATPointsRow {

	private final String category;
	private final String goatPoints;

	public PerformanceGOATPointsRow(String category, String goatPoints) {
		this.category = category;
		this.goatPoints = goatPoints;
	}

	public String getCategory() {
		return category;
	}

	public String getGoatPoints() {
		return goatPoints;
	}
}
