package org.strangeforest.tcb.stats.model;

public class PerfStatGOATPointsRow {

	private final String category;
	private final String goatPoints;

	public PerfStatGOATPointsRow(String category, String goatPoints) {
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
