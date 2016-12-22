 package org.strangeforest.tcb.stats.prediction;

 import java.time.*;
 import java.util.*;
 import java.util.function.*;

 import org.springframework.boot.test.context.*;
 import org.springframework.test.context.*;
 import org.strangeforest.tcb.stats.model.prediction.PredictionArea;
 import org.strangeforest.tcb.stats.model.prediction.*;
 import org.testng.annotations.*;

 import static org.strangeforest.tcb.stats.model.prediction.PredictionArea.*;

 @ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionTuningIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2005, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();
	private static final double PREDICTION_RATE_DELTA = 1.0;

	@Test
	public void tunePredictionByArea() throws InterruptedException {
		setWeights(1.0);
		tunePrediction(PredictionTuningIT::toggleNextArea);
	}

	@Test
	public void tunePredictionByItem() throws InterruptedException {
		setWeights(1.0);
		tunePrediction(PredictionTuningIT::toggleNextItem);
	}

	@Test
	public void tunePredictionInRankingArea() throws InterruptedException {
		tunePredictionInArea(RANKING);
	}

	@Test
	public void tunePredictionInH2HArea() throws InterruptedException {
		tunePredictionInArea(H2H);
	}

	@Test
	public void tunePredictionInWinningPctArea() throws InterruptedException {
		tunePredictionInArea(WINNING_PCT);
	}

	private void tunePredictionInArea(PredictionArea area) throws InterruptedException {
		setWeights(0.0);
		area.setWeights(1.0);
		tunePrediction(configs -> toggleNextAreaItem(area, configs));
	}

	private void tunePrediction(Predicate<Set<Properties>> toggle) throws InterruptedException {
		Map<Properties, PredictionResult> results = new HashMap<>();
		PredictionResult bestResult = verifyPrediction(FROM_DATE, TO_DATE);
		results.put(bestResult.getConfig(), bestResult);
		System.out.println("***** Starting result: " + bestResult);
		Properties currentConfig = bestResult.getConfig();
		int step = 0;
		while (toggle.test(results.keySet())) {
			System.out.println("Tuning step " + (++step));
			PredictionResult result = verifyPrediction(FROM_DATE, TO_DATE);
			results.put(result.getConfig(), result);
			if (result.getPredictionRate() > bestResult.getPredictionRate()) {
				currentConfig = result.getConfig();
				bestResult = result;
				System.out.println("***** New best result: " + bestResult);
			}
			else if (result.getPredictionRate() > bestResult.getPredictionRate() - PREDICTION_RATE_DELTA)
				currentConfig = result.getConfig();
			else
				PredictionConfig.set(currentConfig);
		}
		System.out.println("***** Best result: " + bestResult);
		PredictionConfig.set(bestResult.getConfig());
		printWeights();
	}

	private static boolean toggleNextArea(Set<Properties> configs) {
		for (PredictionArea area : values()) {
			toggle(area);
			if (isNewConfig(configs))
				return true;
			else
				toggle(area);
		}
		return false;
	}

	private static boolean toggleNextItem(Set<Properties> configs) {
		for (PredictionArea area : values())
			if (toggleNextAreaItem(area, configs))
				return true;
		return false;
	}

	private static boolean toggleNextAreaItem(PredictionArea area, Set<Properties> configs) {
		for (PredictionItem item : area.getItems()) {
			toggle(item);
			if (isNewConfig(configs))
				return true;
			else
				toggle(item);
		}
		return false;
	}

	private static void toggle(Weighted weighted) {
		weighted.setWeight(weighted.getWeight() > 0.0 ? 0.0 : 1.0);
	}

	private static boolean isNewConfig(Set<Properties> configs) {
		return isAnyEnabled() && !configs.contains(PredictionConfig.get());
	}
}
