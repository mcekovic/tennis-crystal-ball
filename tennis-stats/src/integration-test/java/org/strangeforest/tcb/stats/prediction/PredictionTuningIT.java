package org.strangeforest.tcb.stats.prediction;

import java.time.*;
import java.util.function.*;
import java.util.stream.*;

import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
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
	private static final double MIN_WEIGHT = 0.0;
	private static final double MAX_WEIGHT = 10.0;
	private static final double WEIGHT_STEP = 1.0;

	@Test
	public void tunePredictionByArea() throws InterruptedException {
		setWeights(1.0);
		doTunePredictionByArea();
	}

	@Test
	public void tuneDefaultPredictionByArea() throws InterruptedException {
		doTunePredictionByArea();
	}

	private void doTunePredictionByArea() throws InterruptedException {
		tunePrediction(asList(PredictionArea.values()), METRICS);
	}

	@Test
	public void tunePredictionByItem() throws InterruptedException {
		setWeights(1.0);
		doTunePredictionByItem();
	}

	@Test
	public void tuneDefaultPredictionByItem() throws InterruptedException {
		doTunePredictionByItem();
	}

	private void doTunePredictionByItem() throws InterruptedException {
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

	@Test
	public void tunePredictionInRecentFormArea() throws InterruptedException {
		tunePredictionInArea(RECENT_FORM);
	}

	@Test
	public void tuneDefaultPredictionInRankingArea() throws InterruptedException {
		tuneDefaultPredictionInArea(RANKING);
	}

	@Test
	public void tuneDefaultPredictionInH2HArea() throws InterruptedException {
		tuneDefaultPredictionInArea(H2H);
	}

	@Test
	public void tuneDefaultPredictionInWinningPctArea() throws InterruptedException {
		tuneDefaultPredictionInArea(WINNING_PCT);
	}

	@Test
	public void tuneDefaultPredictionInRecentFormArea() throws InterruptedException {
		tuneDefaultPredictionInArea(RECENT_FORM);
	}

	private void tunePredictionInArea(PredictionArea area) throws InterruptedException {
		setWeights(0.0);
		area.setWeights(1.0);
		tunePrediction(asList(area.getItems()), METRICS);
	}

	private void tuneDefaultPredictionInArea(PredictionArea area) throws InterruptedException {
		tunePrediction(asList(area.getItems()), METRICS);
	}

	private void tunePrediction(Iterable<Weighted> features, Function<PredictionResult, Double> metrics) throws InterruptedException {
		TuningContext context = new TuningContext(comparing(metrics), MIN_WEIGHT, MAX_WEIGHT, WEIGHT_STEP);
		PredictionResult result = verifyPrediction(FROM_DATE, TO_DATE);

		for (context.initialResult(result); context.startStep() != null; context.endStep()) {
			for (Weighted weighted : features) {
				if (context.stepDown(weighted))
					context.nextResult(verifyPrediction(FROM_DATE, TO_DATE));
				if (context.stepUp(weighted))
					context.nextResult(verifyPrediction(FROM_DATE, TO_DATE));
			}
		}
		context.finish();
	}
}
