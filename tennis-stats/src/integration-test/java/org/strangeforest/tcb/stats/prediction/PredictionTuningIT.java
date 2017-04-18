 package org.strangeforest.tcb.stats.prediction;

 import java.time.*;
 import java.util.*;
 import java.util.function.*;
 import java.util.stream.*;

 import org.springframework.boot.test.context.*;
 import org.springframework.test.context.*;
 import org.strangeforest.tcb.stats.model.prediction.PredictionArea;
 import org.strangeforest.tcb.stats.model.prediction.*;
 import org.testng.annotations.*;

 import static java.util.Arrays.*;
 import static java.util.Comparator.*;
 import static java.util.stream.Collectors.*;
 import static org.strangeforest.tcb.stats.model.prediction.PredictionArea.*;

 @ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionTuningIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2005, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();
	private static final Function<PredictionResult, Double> METRICS = PredictionResult::getPredictablePredictionRate;

	@Test
	public void tunePredictionByArea() throws InterruptedException {
		setWeights(1.0);
		tunePrediction(asList(PredictionArea.values()), METRICS);
	}

	@Test
	public void tunePredictionByItem() throws InterruptedException {
		setWeights(1.0);
		tunePrediction(Stream.of(PredictionArea.values()).flatMap(area -> Stream.of(area.getItems())).collect(toList()), METRICS);
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
		tunePrediction(asList(area.getItems()), METRICS);
	}

	private void tunePrediction(Iterable<Weighted> features, Function<PredictionResult, Double> metrics) throws InterruptedException {
		Map<Properties, PredictionResult> results = new HashMap<>();
		PredictionResult bestResult = verifyPrediction(FROM_DATE, TO_DATE);
		results.put(bestResult.getConfig(), bestResult);
		System.out.println("***** Starting result: " + bestResult);
		Properties currentConfig = bestResult.getConfig();
		int stepCount = 0;
		while (true) {
			PredictionResult bestStepResult = null;
			for (Weighted weighted : features) {
				PredictionConfig.set(currentConfig);
				if (toggle(weighted, results.keySet())) {
					PredictionResult result = verifyPrediction(FROM_DATE, TO_DATE);
					results.put(result.getConfig(), result);

					if (comparing(metrics).compare(result, bestResult) > 0) {
						bestResult = result;
						System.out.println("***** New best result: " + bestResult);
					}
					if (bestStepResult == null || comparing(metrics).compare(result, bestStepResult) > 0)
						bestStepResult = result;
				}
			}
			if (bestStepResult != null) {
				currentConfig = bestStepResult.getConfig();
				System.out.println("*** Tuning step " + (++stepCount) + " finished: " + bestStepResult);
			}
			else
				break;
		}
		System.out.println("***** Best result: " + bestResult);
		PredictionConfig.set(bestResult.getConfig());
		printWeights();
	}

	private static boolean toggle(Weighted weighted, Set<Properties> configs) {
		toggle(weighted);
		if (isNewConfig(configs))
			return true;
		else {
			toggle(weighted);
			return false;
		}
	}

	private static void toggle(Weighted weighted) {
		weighted.setWeight(weighted.getWeight() > 0.0 ? 0.0 : 1.0);
	}

	private static boolean isNewConfig(Set<Properties> configs) {
		return PredictionArea.isAnyEnabled() && !configs.contains(PredictionConfig.get());
	}
}
