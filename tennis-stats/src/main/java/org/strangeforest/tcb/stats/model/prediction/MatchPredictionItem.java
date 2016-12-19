package org.strangeforest.tcb.stats.model.prediction;

public enum MatchPredictionItem implements PredictionItem {

	OVERALL(1.0, false),
	SURFACE(2.0, false),
	LEVEL(1.0, false),
	ROUND(0.5, false),
	RECENT(1.0, false),
	SURFACE_RECENT(2.0, false),
	SET(1.0, true),
	SURFACE_SET(2.0, true);

	private final double weight;
	private final boolean forSet;

	MatchPredictionItem(double weight, boolean forSet) {
		this.weight = weight;
		this.forSet = forSet;
	}

	@Override public double weight() {
		return weight;
	}

	public boolean forSet() {
		return forSet;
	}
}
