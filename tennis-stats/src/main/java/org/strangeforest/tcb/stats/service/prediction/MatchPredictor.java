package org.strangeforest.tcb.stats.service.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

public interface MatchPredictor {

	PredictionArea area();
	MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf);
}
