package org.strangeforest.tcb.stats.model;

public class PerfStatGOATPoints {

	private final String categoryId;
	private final String category;
	private final String goatPoints;

	public PerfStatGOATPoints(String categoryId, String category, String goatPoints) {
		this.categoryId = categoryId;
		this.category = category;
		this.goatPoints = goatPoints;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getCategory() {
		return category;
	}

	public String getGoatPoints() {
		return goatPoints;
	}
}
