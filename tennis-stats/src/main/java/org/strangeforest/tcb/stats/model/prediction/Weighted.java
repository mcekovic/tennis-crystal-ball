package org.strangeforest.tcb.stats.model.prediction;

public interface Weighted {

	double getWeight(PredictionConfig config);
	PredictionConfig setWeight(PredictionConfig config, double weight);

	double minWeight();
	double maxWeight();
	double weightStep();
}
