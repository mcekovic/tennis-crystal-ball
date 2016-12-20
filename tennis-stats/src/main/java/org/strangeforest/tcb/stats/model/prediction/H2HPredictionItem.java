package org.strangeforest.tcb.stats.model.prediction;

public enum H2HPredictionItem implements MatchPredictionItem {

	MATCH(1.0, false),
	SURFACE(1.0, false),
	LEVEL(1.0, false),
	ROUND(1.0, false),
	RECENT(1.0, false),
	SURFACE_RECENT(1.0, false),
	LEVEL_RECENT(0.0, false),
	ROUND_RECENT(0.0, false),
	SET(1.0, true),
	SURFACE_SET(1.0, true),
	LEVEL_SET(0.0, true),
	ROUND_SET(0.0, true),
	RECENT_SET(0.0, true),
	SURFACE_RECENT_SET(0.0, true),
	LEVEL_RECENT_SET(0.0, true),
	ROUND_RECENT_SET(0.0, true);

	private final double weight;
	private final boolean forSet;

	H2HPredictionItem(double weight, boolean forSet) {
		this.weight = weight;
		this.forSet = forSet;
	}

	@Override public double weight() {
		return weight;
	}

	@Override public boolean forSet() {
		return forSet;
	}
}
