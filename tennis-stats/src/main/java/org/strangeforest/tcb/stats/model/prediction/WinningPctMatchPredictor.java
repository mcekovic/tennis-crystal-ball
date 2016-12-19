package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

public class WinningPctMatchPredictor extends MatchDataMatchPredictor {

	public WinningPctMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		super(bestOf, level, matchData2, playerId2, date, matchData1, playerId1, round, surface);
	}

	@Override public PredictionArea area() {
		return PredictionArea.WINNING_PCT;
	}

	protected void addItemProbabilities(MatchPrediction prediction, MatchPredictionItem item, Predicate<MatchData> filter) {
		ToIntFunction<MatchData> wonDimension = item.forSet() ? MatchData::getPSets : MatchData::getPMatches;
		ToIntFunction<MatchData> lostDimension = item.forSet() ? MatchData::getOSets : MatchData::getOMatches;
		long won1 = matchData1.stream().filter(filter).mapToInt(wonDimension).sum();
		long lost1 = matchData1.stream().filter(filter).mapToInt(lostDimension).sum();
		long won2 = matchData2.stream().filter(filter).mapToInt(wonDimension).sum();
		long lost2 = matchData2.stream().filter(filter).mapToInt(lostDimension).sum();
		long total1 = won1 + lost1;
		long total2 = won2 + lost2;
		if (total1 > 0 && total2 > 0) {
			double weight = item.weight() * weight(total1, total2);
			double p1 = 1.0 * won1 / total1;
			double p2 = 1.0 * won2 / total2;
			double p12 = p1 + p2;
			if (p12 > 0.0) {
				DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item);
				prediction.addItemProbability1(area(), item, weight, probabilityTransformer.applyAsDouble(p1 / p12));
				prediction.addItemProbability2(area(), item, weight, probabilityTransformer.applyAsDouble(p2 / p12));
			}
		}
	}
}
