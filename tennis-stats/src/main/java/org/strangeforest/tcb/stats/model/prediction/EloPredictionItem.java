package org.strangeforest.tcb.stats.model.prediction;

public enum EloPredictionItem implements PredictionItem {

	ELO(1.0),
	SURFACE_ELO(2.0);

	private final double weight;

	EloPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public double weight() {
		return weight;
	}
}
