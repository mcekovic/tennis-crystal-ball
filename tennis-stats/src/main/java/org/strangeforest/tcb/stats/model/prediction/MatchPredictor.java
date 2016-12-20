package org.strangeforest.tcb.stats.model.prediction;

public interface MatchPredictor {

	PredictionArea getArea();
	MatchPrediction predictMatch();
}
