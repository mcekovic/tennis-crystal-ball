package org.strangeforest.tcb.stats.model.prediction;

public enum WinningPctPredictionItem implements PredictionItem {

	OVERALL(false, true),
	SURFACE(false, true),
	LEVEL(false, false),
	TOURNAMENT(false, false),
	ROUND(false, true),
	RECENT(false, true),
	SURFACE_RECENT(false, true),
	LEVEL_RECENT(false, false),
	ROUND_RECENT(false, true),
	VS_RANK(false, true),
	VS_HAND(false, true),
	VS_BACKHAND(false, true),
	OVERALL_SET(true, false),
	SURFACE_SET(true, false),
	LEVEL_SET(true, false),
	TOURNAMENT_SET(true, false),
	ROUND_SET(true, false),
	RECENT_SET(true, false),
	SURFACE_RECENT_SET(true, false),
	LEVEL_RECENT_SET(true, false),
	ROUND_RECENT_SET(true, false),
	VS_RANK_SET(true, false),
	VS_HAND_SET(true, false),
	VS_BACKHAND_SET(true, false);

	private volatile PredictionArea area;
	private final boolean forSet;
	private final boolean mixedBestOf;

	WinningPctPredictionItem(boolean forSet, boolean mixedBestOf) {
		this.forSet = forSet;
		this.mixedBestOf = mixedBestOf;
	}

	@Override public String longName() {
		return area + "[" + super.toString() + "]";
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

	public boolean isMixedBestOf() {
		return mixedBestOf;
	}

	@Override public double getWeight(PredictionConfig config) {
		return config.getItemWeight(this);
	}

	@Override public PredictionConfig setWeight(PredictionConfig config, double weight) {
		return new PredictionConfig(config, this, weight);
	}

	@Override public double minWeight() {
		return 0.0;
	}

	@Override public double maxWeight() {
		return 10.0;
	}

	@Override public double weightStep() {
		return 1.0;
	}
}
