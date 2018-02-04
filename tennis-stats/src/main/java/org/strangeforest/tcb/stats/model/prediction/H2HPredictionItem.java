package org.strangeforest.tcb.stats.model.prediction;

public enum H2HPredictionItem implements PredictionItem {

	OVERALL(false),
	SURFACE(false),
	LEVEL(false),
	TOURNAMENT(false),
	ROUND(false),
	RECENT(false),
	SURFACE_RECENT(false),
	LEVEL_RECENT(false),
	ROUND_RECENT(false),
	OVERALL_SET(true),
	SURFACE_SET(true),
	LEVEL_SET(true),
	TOURNAMENT_SET(true),
	ROUND_SET(true),
	RECENT_SET(true),
	SURFACE_RECENT_SET(true),
	LEVEL_RECENT_SET(true),
	ROUND_RECENT_SET(true);

	private volatile PredictionArea area;
	private final boolean forSet;

	H2HPredictionItem(boolean forSet) {
		this.forSet = forSet;
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

	@Override public double getWeight(PredictionConfig config) {
		return config.getItemWeight(this);
	}

	@Override public PredictionConfig setWeight(PredictionConfig config, double weight) {
		return new PredictionConfig(config, this, weight);
	}

	@Override public String toString() {
		return area + "[" + super.toString() + "]";
	}
}
