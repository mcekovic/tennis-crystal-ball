package org.strangeforest.tcb.stats.model.prediction;

public enum RankingPredictionItem implements PredictionItem {

	RANK(false, 0.0),
	RANK_POINTS(false, 4.0),
	ELO(false, 8.0),
	SURFACE_ELO(false, 10.0),
	OUT_IN_ELO(false, 1.0),
	SET_ELO(true, 0.0);

	private volatile PredictionArea area;
	private final boolean forSet;
	private volatile double weight;

	RankingPredictionItem(boolean forSet, double weight) {
		this.weight = weight;
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
