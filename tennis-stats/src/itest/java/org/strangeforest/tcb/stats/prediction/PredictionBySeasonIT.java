package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class PredictionBySeasonIT extends BasePredictionVerificationIT {

	private static final int FROM_YEAR = 2005;
	private static final int TO_YEAR = LocalDate.now().getYear();

	@Test
	void bySeasonPredictions() throws InterruptedException {
		for (var year = FROM_YEAR; year <= TO_YEAR ; year++) {
			var result = verifyPrediction(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
			printResultDistribution(result);
		}
	}
}
