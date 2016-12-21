package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.prediction.*;

import static com.google.common.base.MoreObjects.*;

public class PredictionResult implements Comparable<PredictionResult> {

	private final double predictablePct;
	private final double predictionRate;
	private final Properties config;

	public PredictionResult(double predictablePct, double predictionRate) {
		this.predictablePct = predictablePct;
		this.predictionRate = predictionRate;
		config = PredictionConfig.get();
	}

	public double getPredictablePct() {
		return predictablePct;
	}

	public double getPredictionRate() {
		return predictionRate;
	}

	public Properties getConfig() {
		return config;
	}


	// Object methods

	@Override public String toString() {
		return toStringHelper(this)
			.add("predictablePct", predictablePct)
			.add("predictionRate", predictionRate)
			.add("config", config)
			.toString();
	}

	@Override public int compareTo(PredictionResult result) {
		return Double.compare(predictionRate, result.getPredictionRate());
	}
}
