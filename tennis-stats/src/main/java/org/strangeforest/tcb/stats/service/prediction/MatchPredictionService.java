package org.strangeforest.tcb.stats.service.prediction;

import java.util.*;
import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

@Service
public class MatchPredictionService {

	@Autowired private ApplicationContext context;
	private Iterable<MatchPredictor> matchPredictors;

	@PostConstruct
	private void init() {
		matchPredictors = context.getBeansOfType(MatchPredictor.class).values();
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		MatchPrediction prediction = new MatchPrediction();
		for (MatchPredictor predictor : matchPredictors)
			prediction.addPrediction(predictor.predictMatch(playerId1, playerId2, date, surface, level, round, bestOf), predictor.area().weight());
		return prediction;
	}
}
