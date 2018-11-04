package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

class PredictionScoringIT {

	private Random rnd = new Random();

	@Test
	void idealPredictionScore() {
		for (double p = 0.0; p <= 1.001; p += 0.05) {
			PredictionResult result = new PredictionResult(PredictionConfig.defaultConfig());
			for (int i = 0; i < 1000000; i++) {
				double v = rnd.nextDouble();
				boolean predicted = v < p;
				result.newMatch(v != p, predicted ? p : 1 - p, predicted, false, false, false, 0.0, 0.0);
			}
			result.complete();
			System.out.printf("%1$.2f: %2$s\n", p, result);
		}
	}

	@Test
	void predictionScore() {
		for (double p = 0.0; p <= 1.0; p += 0.1) {
			for (double pp = 0.0; pp <= 1.0; pp += 0.1) {
				PredictionResult result = new PredictionResult(PredictionConfig.defaultConfig());
				for (int i = 0; i < 1000000; i++) {
					double v = rnd.nextDouble();
					boolean predicted = v < p;
					result.newMatch(v != pp, predicted ? pp : 1 - pp, predicted, false, false, false, 0.0, 0.0);
				}
				result.complete();
				System.out.printf("%1$.2f, %2$.2f: %3$s\n", p, pp, result);
			}
			System.out.println();
		}
	}
}
