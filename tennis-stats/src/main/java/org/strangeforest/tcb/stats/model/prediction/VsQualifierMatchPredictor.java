package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.model.prediction.H2HMatchPredictor.*;
import static org.strangeforest.tcb.stats.model.prediction.H2HPredictionItem.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class VsQualifierMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData;
	private final Date date;
	private final Surface surface;
	private final TournamentLevel level;
	private final Integer tournamentId;
	private final Round round;
	private final short bestOf;

	public VsQualifierMatchPredictor(List<MatchData> matchData, Date date, Surface surface, TournamentLevel level, Integer tournamentId, Round round, short bestOf) {
		this.matchData = matchData;
		this.date = date;
		this.surface = surface;
		this.level = level;
		this.tournamentId = tournamentId;
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
		addItemProbabilities(prediction, TOURNAMENT, isTournament(tournamentId));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date, getMatchRecentPeriod()));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date, getMatchRecentPeriod())));
		addItemProbabilities(prediction, SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, TOURNAMENT_SET, isTournament(tournamentId));
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
			ToIntFunction<MatchData> dimension = item.isForSet() ? MatchData::getSets : MatchData::getMatches;

			Predicate<MatchData> qualifierFilter = filter.and(isOpponentQualifier());
			int total = matchData.stream().filter(qualifierFilter).mapToInt(dimension).sum();
			if (total > 0) {
				ToIntFunction<MatchData> pDimension = item.isForSet() ? MatchData::getPSets : MatchData::getPMatches;
				int won = matchData.stream().filter(qualifierFilter).mapToInt(pDimension).sum();
				int lost = total - won;
				double weight = item.getWeight() * weight(total);
				DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item.isForSet(), bestOf);
				prediction.addItemProbability1(getArea(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won / total));
				prediction.addItemProbability2(getArea(), item, weight, probabilityTransformer.applyAsDouble(1.0 * lost / total));
			}
		}
	}
}
