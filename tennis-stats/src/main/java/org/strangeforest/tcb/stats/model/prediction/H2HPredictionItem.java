package org.strangeforest.tcb.stats.model.prediction;

public enum H2HPredictionItem implements PredictionItem {

	H2H(1.0),
	SURFACE_H2H(2.0),
	LEVEL_H2H(1.0),
	ROUND_H2H(0.5),
	RECENT_H2H(1.0),
	SURFACE_RECENT_H2H(2.0),
	SET_H2H(1.0),
	SURFACE_SET_H2H(2.0);

	private final double weight;

	H2HPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public double weight() {
		return weight;
	}
}
