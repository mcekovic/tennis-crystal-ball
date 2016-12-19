package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

public class H2HMatchPredictor extends MatchDataMatchPredictor {

	public H2HMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		super(bestOf, level, matchData2, playerId2, date, matchData1, playerId1, round, surface);
	}

	@Override public PredictionArea area() {
		return PredictionArea.H2H;
	}

	protected void addItemProbabilities(MatchPrediction prediction, MatchPredictionItem item, Predicate<MatchData> filter) {
		ToIntFunction<MatchData> dimension = item.forSet() ? MatchData::getPSets : MatchData::getPMatches;
		long won1 = matchData1.stream().filter(filter.and(isOpponent(playerId2))).mapToInt(dimension).sum();
		long won2 = matchData2.stream().filter(filter.and(isOpponent(playerId1))).mapToInt(dimension).sum();
		long total = won1 + won2;
		if (total > 0) {
			double weight = item.weight() * weight(total);
			DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item);
			prediction.addItemProbability1(area(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won1 / total));
			prediction.addItemProbability2(area(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won2 / total));
		}
	}

	private static Predicate<MatchData> isOpponent(int playerId) {
		return m -> m.getOpponentId() == playerId;
	}
}
