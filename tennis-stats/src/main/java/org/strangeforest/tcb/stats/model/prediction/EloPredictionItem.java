package org.strangeforest.tcb.stats.model.prediction;

public enum EloPredictionItem implements PredictionItem, Weighted {

	OVERALL(1.0),
	SURFACE(1.0);

	private final double weight;

	EloPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public double weight() {
		return weight;
	}
}
