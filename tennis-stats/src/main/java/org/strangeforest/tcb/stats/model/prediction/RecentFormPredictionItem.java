package org.strangeforest.tcb.stats.model.prediction;

public enum RecentFormPredictionItem implements PredictionItem {

	OVERALL(),
	SURFACE(),
	LEVEL(),
	ROUND(),
	VS_RANK(),
	VS_HAND(),
	VS_BACKHAND();

	private volatile PredictionArea area;

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
		return false;
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
