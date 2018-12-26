package org.strangeforest.tcb.stats.prediction;

import java.io.*;
import java.time.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static java.util.Arrays.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.PredictionArea.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class PredictionTuningIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2005, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();
	private static final TuningSet TUNING_SET = TuningSet.OVERALL;
	private static final Function<PredictionResult, Double> METRICS = PredictionResult::getScore;
	private static final boolean SAVE_BEST_CONFIG = false;


	// Starting from default weights

	@Test
	void tuneDefaultPrediction() throws InterruptedException {
		doTunePrediction(PredictionConfig.defaultConfig(TUNING_SET), null);
	}

	@Test
	void tuneDefaultPredictionByArea() throws InterruptedException {
		doTunePredictionByArea(PredictionConfig.defaultConfig(TUNING_SET), null);
	}

	@Test
	void tuneDefaultPredictionByItem() throws InterruptedException {
		doTunePredictionByItem(PredictionConfig.defaultConfig(TUNING_SET), null);
	}

	@Test
	void tuneDefaultPredictionInRankingArea() throws InterruptedException {
		doTunePredictionInArea(PredictionConfig.defaultConfig(TUNING_SET), RANKING, null);
	}

	@Test
	void tuneDefaultPredictionInRecentFormArea() throws InterruptedException {
		doTunePredictionInArea(PredictionConfig.defaultConfig(TUNING_SET), RECENT_FORM, null);
	}

	@Test
	void tuneDefaultPredictionInH2HArea() throws InterruptedException {
		doTunePredictionInArea(PredictionConfig.defaultConfig(TUNING_SET), H2H, null);
	}

	@Test
	void tuneDefaultPredictionInWinningPctArea() throws InterruptedException {
		doTunePredictionInArea(PredictionConfig.defaultConfig(TUNING_SET), WINNING_PCT, null);
	}

	@Test
	void scriptedTuneDefaultPrediction() throws InterruptedException {
		int factor = 100;
		PredictionConfig config = PredictionConfig.defaultConfig(TUNING_SET);
		System.out.println("Tuning RANKING area...");
		config = doTunePredictionInArea(config, RANKING, 5 * factor).getConfig();
		System.out.println("Tuning RECENT_FORM area...");
		config = doTunePredictionInArea(config, RECENT_FORM, 5 * factor).getConfig();
		System.out.println("Tuning H2H area...");
		config = doTunePredictionInArea(config, H2H, 3 * factor).getConfig();
		System.out.println("Tuning WINNING_PCT area...");
		config = doTunePredictionInArea(config, WINNING_PCT, 2 * factor).getConfig();
		System.out.println("Tuning by area...");
		doTunePredictionByArea(config, 10 * factor);
	}


	// Starting from equal weights

	@Test @Disabled
	void tunePrediction() throws InterruptedException {
		doTunePrediction(PredictionConfig.equalWeights(), null);
	}

	@Test @Disabled
	void tunePredictionByArea() throws InterruptedException {
		doTunePredictionByArea(PredictionConfig.equalWeights(), null);
	}

	@Test @Disabled
	void tunePredictionByItem() throws InterruptedException {
		doTunePredictionByItem(PredictionConfig.equalWeights(), null);
	}

	@Test @Disabled
	void tunePredictionInRankingArea() throws InterruptedException {
		doTunePredictionInAreaFromPointZero(RANKING, null);
	}

	@Test @Disabled
	void tunePredictionInRecentFormArea() throws InterruptedException {
		doTunePredictionInAreaFromPointZero(RECENT_FORM, null);
	}

	@Test @Disabled
	void tunePredictionInH2HArea() throws InterruptedException {
		doTunePredictionInAreaFromPointZero(H2H, null);
	}

	@Test @Disabled
	void tunePredictionInWinningPctArea() throws InterruptedException {
		doTunePredictionInAreaFromPointZero(WINNING_PCT, null);
	}


	// Tuning

	private PredictionResult doTunePrediction(PredictionConfig config, Integer maxSteps) throws InterruptedException {
		return tunePrediction(config,  Stream.of(PredictionArea.values()).flatMap(area -> Stream.of(area.getAreaAndItems())).collect(toList()), METRICS, maxSteps);
	}

	private PredictionResult doTunePredictionByArea(PredictionConfig config, Integer maxSteps) throws InterruptedException {
		return tunePrediction(config, asList(PredictionArea.values()), METRICS, maxSteps);
	}

	private PredictionResult doTunePredictionByItem(PredictionConfig config, Integer maxSteps) throws InterruptedException {
		return tunePrediction(config, Stream.of(PredictionArea.values()).flatMap(area -> Stream.of(area.getItems())).collect(toList()), METRICS, maxSteps);
	}

	private PredictionResult doTunePredictionInAreaFromPointZero(PredictionArea area, Integer maxSteps) throws InterruptedException {
		return tunePrediction(PredictionConfig.areaEqualWeights(area), asList(area.getItems()), METRICS, maxSteps);
	}

	private PredictionResult doTunePredictionInArea(PredictionConfig config, PredictionArea area, Integer maxSteps) throws InterruptedException {
		return tunePrediction(config, asList(area.getItems()), METRICS, maxSteps);
	}

	private PredictionResult tunePrediction(PredictionConfig config, Iterable<Weighted> features, Function<PredictionResult, Double> metrics, Integer maxSteps) throws InterruptedException {
		TuningContext context = new TuningContext(comparing(metrics));
		PredictionVerificationResult result = verifyPrediction(FROM_DATE, TO_DATE, config, TUNING_SET);

		for (context.initialResult(result); (maxSteps == null || context.currentStep() <= maxSteps) && context.startStep() != null; context.endStep()) {
			for (Weighted weighted : features) {
				PredictionConfig stepDownConfig = context.stepDown(weighted);
				if (stepDownConfig != null)
					tuningStep(context, stepDownConfig);
				PredictionConfig stepUpConfig = context.stepUp(weighted);
				if (stepUpConfig != null)
					tuningStep(context, stepUpConfig);
			}
		}
		context.finish();
		return context.getBestResult();
	}

	private void tuningStep(TuningContext context, PredictionConfig config) throws InterruptedException {
		PredictionVerificationResult result = verifyPrediction(FROM_DATE, TO_DATE, config, TUNING_SET);
		if (context.nextResult(result)) {
			printWeights(config, false);
			printResultDistribution(result);
			if (SAVE_BEST_CONFIG) {
				try (PrintStream out = new PrintStream(new FileOutputStream("src/main/resources" + PredictionConfig.getConfigFileName(TUNING_SET)))) {
					out.println("# TENNIS CRYSTAL BALL - " + TUNING_SET);
					out.println("# " + result.getResult());
					out.println("# " + result.getProbabilityRangeResults());
					out.println("# " + result.getSurfaceResults());
					out.println("# " + result.getLevelResults());
					out.println("# " + result.getBestOfResults());
					out.println("# " + result.getRankRangeResults());
					out.println("# Tuned at: " + LocalDateTime.now());
					out.println();
					config.save(out);
					out.flush();
				}
				catch (FileNotFoundException ex) {
					throw new IllegalArgumentException("Cannot save config", ex);
				}
			}
		}
	}
}
