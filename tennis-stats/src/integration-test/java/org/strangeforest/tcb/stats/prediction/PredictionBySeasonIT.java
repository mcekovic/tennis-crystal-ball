package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class PredictionBySeasonIT extends BasePredictionVerificationIT {

	private static final int FROM_YEAR = 2005;
	private static final int TO_YEAR = LocalDate.now().getYear();

	@Test
	void bySeasonPredictions() throws InterruptedException {
		for (int year = FROM_YEAR; year <= TO_YEAR ; year++) {
			PredictionVerificationResult result = verifyPrediction(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
			printResultDistribution(result);
		}
	}
}
