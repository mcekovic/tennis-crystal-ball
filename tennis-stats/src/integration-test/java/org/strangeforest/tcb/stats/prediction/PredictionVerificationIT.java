package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.testng.annotations.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
@Test(sequential = true)
public class PredictionVerificationIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2000, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();

	@Test
	public void verifyDefaultPrediction() throws InterruptedException {
		verifyPrediction(FROM_DATE, TO_DATE);
	}

	@Test
	public void allAreasAllItemsPredictions() throws InterruptedException {
		resetWeights(1.0);
		verifyPrediction(FROM_DATE, TO_DATE);
	}

	@Test
	public void singleAreaAllItemsPredictions() throws InterruptedException {
		for (PredictionArea area : PredictionArea.values()) {
			resetWeights(0.0, 1.0);
			area.setWeight(1.0);
			verifyPrediction(FROM_DATE, TO_DATE);
		}
	}

	@Test
	public void singleItemPredictions() throws InterruptedException {
		for (PredictionArea area : PredictionArea.values()) {
			for (PredictionItem item : area.getItems()) {
				resetWeights(0.0, 0.0);
				item.setWeight(1.0);
				area.setWeight(1.0);
				verifyPrediction(FROM_DATE, TO_DATE);
			}
		}
	}
}
