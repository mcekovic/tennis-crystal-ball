package org.strangeforest.tcb.stats.model.prediction;

public enum H2HPredictionItem implements MatchPredictionItem {

	OVERALL(false, 0.0),
	SURFACE(false, 3.0),
	LEVEL(false, 2.0),
	TOURNAMENT(false, 2.0),
	ROUND(false, 3.0),
	RECENT(false, 4.0),
	SURFACE_RECENT(false, 2.0),
	LEVEL_RECENT(false, 1.0),
	ROUND_RECENT(false, 2.0),
	OVERALL_SET(true, 2.0),
	SURFACE_SET(true, 3.0),
	LEVEL_SET(true, 2.0),
	TOURNAMENT_SET(true, 2.0),
	ROUND_SET(true, 0.0),
	RECENT_SET(true, 0.0),
	SURFACE_RECENT_SET(true, 2.0),
	LEVEL_RECENT_SET(true, 0.0),
	ROUND_RECENT_SET(true, 0.0);

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

	@Override public String toString() {
		return area + "[" + super.toString() + "]";
	}
}
