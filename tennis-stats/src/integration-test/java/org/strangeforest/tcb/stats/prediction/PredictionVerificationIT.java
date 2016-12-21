package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.testng.annotations.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionVerificationIT extends BasePredictionVerificationIT {

	@Test
	public void verifyPrediction() throws InterruptedException {
		verifyPrediction(LocalDate.of(2000, 1, 1), LocalDate.now());
	}
}
