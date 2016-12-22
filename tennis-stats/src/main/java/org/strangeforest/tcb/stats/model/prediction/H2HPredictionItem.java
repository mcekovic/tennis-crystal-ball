package org.strangeforest.tcb.stats.model.prediction;

public enum H2HPredictionItem implements MatchPredictionItem {

	MATCH(false, 0.0),
	SURFACE(false, 0.0),
	LEVEL(false, 0.0),
	ROUND(false, 1.0),
	RECENT(false, 0.0),
	SURFACE_RECENT(false, 1.0),
	LEVEL_RECENT(false, 1.0),
	ROUND_RECENT(false, 1.0),
	SET(true, 1.0),
	SURFACE_SET(true, 1.0),
	LEVEL_SET(true, 1.0),
	ROUND_SET(true, 1.0),
	RECENT_SET(true, 1.0),
	SURFACE_RECENT_SET(true, 1.0),
	LEVEL_RECENT_SET(true, 1.0),
	ROUND_RECENT_SET(true, 1.0);

	private volatile PredictionArea area;
	private final boolean forSet;
	private volatile double weight;

	H2HPredictionItem(boolean forSet, double weight) {
		this.forSet = forSet;
		this.weight = weight;
	}

	@Override public PredictionArea getArea() {
		return area;
	}

	@Override public void setArea(PredictionArea area) {
		this.area = area;
	}

	@Override public boolean isForSet() {
		return forSet;
	}

	@Override public double getWeight() {
		return weight;
	}

	@Override public void setWeight(double weight) {
		this.weight = weight;
		area.calculateItemAdjustmentWeight();
	}
}
