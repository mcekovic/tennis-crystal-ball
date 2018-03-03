package org.strangeforest.tcb.stats.model.prediction;

public enum RankingPredictionItem implements PredictionItem {

	RANK(false),
	RANK_POINTS(false),
	ELO(false),
	SURFACE_ELO(false),
	OUT_IN_ELO(false),
	SET_ELO(true);

	private volatile PredictionArea area;
	private final boolean forSet;

	RankingPredictionItem(boolean forSet) {
		this.forSet = forSet;
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
		return 20.0;
	}

	@Override public double weightStep() {
		return 1.0;
	}
}
