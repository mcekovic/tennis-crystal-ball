package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.testng.annotations.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
@Test(sequential = true)
public class PredictionVerificationIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2005, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();

	@Test
	public void verifyDefaultPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE);
	}

	@Test
	public void verifyDefaultRankingPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_RANKING), TuningSet.OVERALL_RANKING);
	}

	@Test
	public void verifyDefaultRecentFormPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_RECENT_FORM), TuningSet.OVERALL_RECENT_FORM);
	}

	@Test
	public void verifyDefaultH2HPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_H2H), TuningSet.OVERALL_H2H);
	}

	@Test
	public void verifyDefaultWinningPctPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_WINNING_PCT), TuningSet.OVERALL_WINNING_PCT);
	}

	@Test
	public void verifyDefaultPredictionForHardOutdoor() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR);
	}

	@Test
	public void verifyDefaultPredictionForClay() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY);
	}

	@Test
	public void verifyDefaultPredictionForGrass() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS);
	}

	@Test
	public void verifyDefaultPredictionForHardIndoorCarpet() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_INDOOR_CARPET);
	}

	@Test
	public void verifyDefaultPredictionForBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.BEST_OF_3);
	}

	@Test
	public void verifyDefaultPredictionForBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.BEST_OF_5);
	}

	@Test
	public void verifyDefaultPredictionForHardOutdoorBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR_BEST_OF_3);
	}

	@Test
	public void verifyDefaultPredictionForHardOutdoorBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR_BEST_OF_5);
	}

	@Test
	public void verifyDefaultPredictionForClayBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY_BEST_OF_3);
	}

	@Test
	public void verifyDefaultPredictionForClayBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY_BEST_OF_5);
	}

	@Test
	public void verifyDefaultPredictionForGrassBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS_BEST_OF_3);
	}

	@Test
	public void verifyDefaultPredictionForGrassBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS_BEST_OF_5);
	}

	@Test
	public void allAreasAllItemsPredictions() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.equalWeights());
	}

	@Test
	public void singleAreaAllItemsPredictions() throws InterruptedException {
		for (PredictionArea area : PredictionArea.values())
			verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.areaEqualWeights(area));
	}

	@Test
	public void singleItemPredictions() throws InterruptedException {
		for (PredictionArea area : PredictionArea.values()) {
			for (PredictionItem item : area.getItems())
				verifyPredictionPrintInfo(FROM_DATE, TO_DATE, new PredictionConfig(area, 1.0, item, 1.0));
		}
	}

	@Test
	public void adHocSingleItemPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, new PredictionConfig(PredictionArea.RANKING, 1.0, RankingPredictionItem.RANK_POINTS, 1.0));
	}


	// Util

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate) throws InterruptedException {
		PredictionVerificationResult result = verifyPrediction(fromDate, toDate);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, TuningSet tuningSet) throws InterruptedException {
		PredictionVerificationResult result = verifyPrediction(fromDate, toDate, tuningSet);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, PredictionConfig config) throws InterruptedException {
		PredictionVerificationResult result = verifyPrediction(fromDate, toDate, config);
		printWeights(config, false);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, PredictionConfig config, TuningSet tuningSet) throws InterruptedException {
		PredictionVerificationResult result = verifyPrediction(fromDate, toDate, config, tuningSet);
		printWeights(config, false);
		printResultDistribution(result);
	}
}
