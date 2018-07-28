package org.strangeforest.tcb.stats.model.forecast;

public enum ForecastEloType {

	OVERALL("Overall", "Elo", ""),
	RECENT("Recent", "Recent Elo", "recent_"),
	SURFACE("Surface", "Surface Elo", "surface_"),
	IN_OUT("In / Out", "In / Out Elo", "in_out_"),
	SET("Set", "Set Elo", "set_");

	private final String text;
	private final String description;
	private final String columnPrefix;

	ForecastEloType(String text, String description, String columnPrefix) {
		this.text = text;
		this.description = description;
		this.columnPrefix = columnPrefix;
	}

	public String getText() {
		return text;
	}

	public String getDescription() {
		return description;
	}

	public String getColumnPrefix() {
		return columnPrefix;
	}
}
