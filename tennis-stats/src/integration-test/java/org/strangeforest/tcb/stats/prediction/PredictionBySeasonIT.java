package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.testng.annotations.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionBySeasonIT extends BasePredictionVerificationIT {

	private static final int FROM_YEAR = 1970;
	private static final int TO_YEAR = LocalDate.now().getYear();

	@Test
	public void bySeasonPredictions() throws InterruptedException {
		for (int year = FROM_YEAR; year <= TO_YEAR ; year++)
			verifyPrediction(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
	}
}
