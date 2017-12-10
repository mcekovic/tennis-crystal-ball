package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;
import org.testng.annotations.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.*;
import static org.strangeforest.tcb.stats.model.core.Round.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;
import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionIT extends AbstractTestNGSpringContextTests {

	@Autowired private PlayerService playerService;
	@Autowired private MatchPredictionService predictionService;

	@Test
	public void novakDjokovicVsRafaelNadalPotentialRolandGarrosFinalPrediction() {
		int playerId1 = playerService.findPlayerId("Novak Djokovic").get();
		int playerId2 = playerService.findPlayerId("Rafael Nadal").get();

		MatchPrediction prediction = predictionService.predictMatch(playerId1, playerId2, LocalDate.now(), CLAY, false, GRAND_SLAM, F);

		System.out.printf("Novak Djokovic win: %1$.2f%%\n", 100.0 * prediction.getWinProbability1());
		System.out.printf("Rafael Nadal win: %1$.2f%%\n", 100.0 * prediction.getWinProbability2());
		assertThat(prediction.getWinProbability1() + prediction.getWinProbability2()).isCloseTo(1.0, withPercentage(0.00001));

		System.out.println(prediction.getItemProbabilities1());
		System.out.println(prediction.getItemProbabilities2());
		System.out.println("Predictability: " + prediction.getPredictability1());
		System.out.println("Predictability: " + prediction.getPredictability2());
		assertThat(prediction.getItemProbabilitiesWeight1()).isEqualTo(prediction.getItemProbabilitiesWeight2());
		assertThat(prediction.getPredictability1()).isLessThanOrEqualTo(1.0);
		assertThat(prediction.getPredictability2()).isLessThanOrEqualTo(1.0);
	}
}
