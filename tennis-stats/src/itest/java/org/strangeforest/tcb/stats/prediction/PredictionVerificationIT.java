package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.model.prediction.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class PredictionVerificationIT extends BasePredictionVerificationIT {

	private static final LocalDate FROM_DATE = LocalDate.of(2005, 1, 1);
	private static final LocalDate TO_DATE = LocalDate.now();

	@Test
	void verifyDefaultPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE);
	}

	@Test
	void verifyDefaultRankingPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_RANKING), TuningSet.OVERALL_RANKING);
	}

	@Test
	void verifyDefaultRecentFormPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_RECENT_FORM), TuningSet.OVERALL_RECENT_FORM);
	}

	@Test
	void verifyDefaultH2HPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_H2H), TuningSet.OVERALL_H2H);
	}

	@Test
	void verifyDefaultWinningPctPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(TuningSet.OVERALL_WINNING_PCT), TuningSet.OVERALL_WINNING_PCT);
	}

	@Test
	void verifyOverallDefaultPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.defaultConfig(), TuningSet.OVERALL);
	}

	@Test
	void verifyDefaultPredictionForHardOutdoor() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR);
	}

	@Test
	void verifyDefaultPredictionForClay() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY);
	}

	@Test
	void verifyDefaultPredictionForGrass() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS);
	}

	@Test
	void verifyDefaultPredictionForHardIndoorCarpet() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_INDOOR_CARPET);
	}

	@Test
	void verifyDefaultPredictionForBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.BEST_OF_3);
	}

	@Test
	void verifyDefaultPredictionForBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.BEST_OF_5);
	}

	@Test
	void verifyDefaultPredictionForHardOutdoorBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR_BEST_OF_3);
	}

	@Test
	void verifyDefaultPredictionForHardOutdoorBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.HARD_OUTDOOR_BEST_OF_5);
	}

	@Test
	void verifyDefaultPredictionForClayBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY_BEST_OF_3);
	}

	@Test
	void verifyDefaultPredictionForClayBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.CLAY_BEST_OF_5);
	}

	@Test
	void verifyDefaultPredictionForGrassBestOf3() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS_BEST_OF_3);
	}

	@Test
	void verifyDefaultPredictionForGrassBestOf5() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, TuningSet.GRASS_BEST_OF_5);
	}

	@Test
	public void allAreasAllItemsPredictions() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.equalWeights());
	}

	@Test
	void singleAreaAllItemsPredictions() throws InterruptedException {
		for (var area : PredictionArea.values())
			verifyPredictionPrintInfo(FROM_DATE, TO_DATE, PredictionConfig.areaEqualWeights(area));
	}

	@Test
	void singleItemPredictions() throws InterruptedException {
		for (var area : PredictionArea.values()) {
			for (var item : area.getItems())
				verifyPredictionPrintInfo(FROM_DATE, TO_DATE, new PredictionConfig(area, 1.0, item, 1.0));
		}
	}

	@Test
	void adHocSingleItemPrediction() throws InterruptedException {
		verifyPredictionPrintInfo(FROM_DATE, TO_DATE, new PredictionConfig(PredictionArea.RANKING, 1.0, RankingPredictionItem.ELO, 1.0));
	}


	// Util

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate) throws InterruptedException {
		var result = verifyPrediction(fromDate, toDate);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, TuningSet tuningSet) throws InterruptedException {
		var result = verifyPrediction(fromDate, toDate, tuningSet);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, PredictionConfig config) throws InterruptedException {
		var result = verifyPrediction(fromDate, toDate, config);
		printWeights(config, false);
		printResultDistribution(result);
	}

	private void verifyPredictionPrintInfo(LocalDate fromDate, LocalDate toDate, PredictionConfig config, TuningSet tuningSet) throws InterruptedException {
		var result = verifyPrediction(fromDate, toDate, config, tuningSet);
		printWeights(config, false);
		printResultDistribution(result);
	}
}
