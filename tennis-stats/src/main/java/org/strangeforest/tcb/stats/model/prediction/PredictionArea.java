package org.strangeforest.tcb.stats.model.prediction;

public enum PredictionArea implements PredictionItem {

	ELO(1.0),
	H2H(1.0),
	WINNING_PCT(1.0);

	private final double weight;

	PredictionArea(double weight) {
		this.weight = weight;
	}

	@Override public double weight() {
		return weight;
	}
}
