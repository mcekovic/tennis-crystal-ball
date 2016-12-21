package org.strangeforest.tcb.stats.model.prediction;

public enum RankingPredictionItem implements PredictionItem, Weighted {

	RANK(1.0),
	ELO(1.0),
	SURFACE_ELO(1.0);

	private volatile double weight;

	RankingPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public double getWeight() {
		return weight;
	}

	@Override public void setWeight(double weight) {
		this.weight = weight;
	}
}
