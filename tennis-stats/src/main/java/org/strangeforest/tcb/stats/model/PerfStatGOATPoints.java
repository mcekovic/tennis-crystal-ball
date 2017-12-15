package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.model.GOATListConfig.*;

public class PerfStatGOATPoints {

	private final String categoryId;
	private final String category;
	private String goatPoints;

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

	public void applyFactor(int factor) {
		applyFactorToCSV(goatPoints, factor);
	}
}
