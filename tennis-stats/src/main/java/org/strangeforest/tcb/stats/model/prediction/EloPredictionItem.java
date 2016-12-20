package org.strangeforest.tcb.stats.model.prediction;

public enum EloPredictionItem implements PredictionItem, Weighted {

	OVERALL(1.0),
	SURFACE(1.0);

	private volatile double weight;

	EloPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public double getWeight() {
		return weight;
	}

	@Override public void setWeight(double weight) {
		this.weight = weight;
	}
}
