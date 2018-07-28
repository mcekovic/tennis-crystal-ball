package org.strangeforest.tcb.stats.model.forecast;

public enum ForecastEloType {

	OVERALL("Overall"),
	RECENT("Recent"),
	SURFACE("Surface"),
	IN_OUT("In / Out"),
	SET("Set");

	private final String text;

	ForecastEloType(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
