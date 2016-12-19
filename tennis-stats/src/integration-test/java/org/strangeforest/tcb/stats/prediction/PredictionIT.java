package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.service.prediction.*;
import org.testng.annotations.*;

import static org.strangeforest.tcb.stats.model.Round.*;
import static org.strangeforest.tcb.stats.model.Surface.*;
import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

@ContextConfiguration(classes = PredictionITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PredictionIT extends AbstractTestNGSpringContextTests {

	@Autowired private PlayerService playerService;
	@Autowired private MatchPredictionService predictionService;

	@Test
	public void testPrediction() {
		int playerId1 = playerService.findPlayerId("Novak Djokovic").get();
		int playerId2 = playerService.findPlayerId("Rafael Nadal").get();
		MatchPrediction prediction = predictionService.predictMatch(playerId1, playerId2, new Date(), CLAY, GRAND_SLAM, F, (short)5);
		System.out.println(prediction.getWinProbability1());
		System.out.println(prediction.getItemProbabilities1());
		System.out.println(prediction.getWinProbability2());
		System.out.println(prediction.getItemProbabilities2());
		System.out.println(prediction.getWinProbability1() + prediction.getWinProbability2());
	}
}
