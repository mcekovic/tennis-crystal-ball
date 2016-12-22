package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.model.prediction.H2HPredictionItem.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class H2HMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final int playerId1;
	private final int playerId2;
	private final Date date;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final short bestOf;

	private static final int MATCH_RECENT_PERIOD_YEARS = 3;
	private static final int SET_RECENT_PERIOD_YEARS = 2;

	public H2HMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.playerId1 = playerId1;
		this.playerId2 = playerId2;
		this.date = date;
		this.surface = surface;
		this.level = level;
		this.round = round;
		this.bestOf = bestOf;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.H2H;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, MATCH, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface));
		addItemProbabilities(prediction, LEVEL, isLevel(level));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date, getMatchRecentPeriod()));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, ROUND_SET, isRound(round));
		addItemProbabilities(prediction, RECENT_SET, isRecent(date, getSetRecentPeriod()));
		addItemProbabilities(prediction, SURFACE_RECENT_SET, isSurface(surface).and(isRecent(date, getSetRecentPeriod())));
		addItemProbabilities(prediction, LEVEL_RECENT_SET, isLevel(level).and(isRecent(date, getSetRecentPeriod())));
		addItemProbabilities(prediction, ROUND_RECENT_SET, isRound(round).and(isRecent(date, getSetRecentPeriod())));
		return prediction;
	}

	private Period getMatchRecentPeriod() {
		return Period.ofYears(PredictionConfig.getIntegerProperty("recent_period.match." + getArea()).orElse(MATCH_RECENT_PERIOD_YEARS));
	}

	private Period getSetRecentPeriod() {
		return Period.ofYears(PredictionConfig.getIntegerProperty("recent_period.set." + getArea()).orElse(SET_RECENT_PERIOD_YEARS));
	}

	private void addItemProbabilities(MatchPrediction prediction, H2HPredictionItem item, Predicate<MatchData> filter) {
		if (item.getWeight() > 0.0) {
			ToIntFunction<MatchData> dimension = item.isForSet() ? MatchData::getPSets : MatchData::getPMatches;
			long won1 = matchData1.stream().filter(filter.and(isOpponent(playerId2))).mapToInt(dimension).sum();
			long won2 = matchData2.stream().filter(filter.and(isOpponent(playerId1))).mapToInt(dimension).sum();
			long total = won1 + won2;
			if (total > 0) {
				double weight = item.getWeight() * weight(total);
				DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item.isForSet(), bestOf);
				prediction.addItemProbability1(getArea(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won1 / total));
				prediction.addItemProbability2(getArea(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won2 / total));
			}
		}
	}
}
