package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.prediction.H2HPredictionItem.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class H2HMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final int playerId1;
	private final int playerId2;
	private final LocalDate date1;
	private final LocalDate date2;
	private final Surface surface;
	private final TournamentLevel level;
	private final Integer tournamentId;
	private final Round round;
	private final short bestOf;
	private final PredictionConfig config;

	static final int MATCH_RECENT_PERIOD_YEARS = 3;
	static final int SET_RECENT_PERIOD_YEARS = 2;

	public H2HMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, int playerId1, int playerId2, LocalDate date1, LocalDate date2,
	                         Surface surface, TournamentLevel level, Integer tournamentId, Round round, short bestOf, PredictionConfig config) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.playerId1 = playerId1;
		this.playerId2 = playerId2;
		this.date1 = date1;
		this.date2 = date2;
		this.surface = surface;
		this.level = level;
		this.tournamentId = tournamentId;
		this.round = round;
		this.bestOf = bestOf;
		this.config = config;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.H2H;
	}

	@Override public MatchPrediction predictMatch() {
		var matchRecentPeriod = getMatchRecentPeriod();
		var setRecentPeriod = getSetRecentPeriod();
		var prediction = new MatchPrediction(config.getTotalAreasWeight(), bestOf);
		addItemProbabilities(prediction, OVERALL, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface));
		addItemProbabilities(prediction, LEVEL, isLevel(level));
		addItemProbabilities(prediction, TOURNAMENT, isTournament(tournamentId));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date1, matchRecentPeriod), isRecent(date2, matchRecentPeriod));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date1, matchRecentPeriod)), isSurface(surface).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date1, matchRecentPeriod)), isLevel(level).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date1, matchRecentPeriod)), isRound(round).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, OVERALL_SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, TOURNAMENT_SET, isTournament(tournamentId));
		addItemProbabilities(prediction, ROUND_SET, isRound(round));
		addItemProbabilities(prediction, RECENT_SET, isRecent(date1, setRecentPeriod), isRecent(date2, setRecentPeriod));
		addItemProbabilities(prediction, SURFACE_RECENT_SET, isSurface(surface).and(isRecent(date1, setRecentPeriod)), isSurface(surface).and(isRecent(date2, setRecentPeriod)));
		addItemProbabilities(prediction, LEVEL_RECENT_SET, isLevel(level).and(isRecent(date1, setRecentPeriod)), isLevel(level).and(isRecent(date2, setRecentPeriod)));
		addItemProbabilities(prediction, ROUND_RECENT_SET, isRound(round).and(isRecent(date1, setRecentPeriod)), isRound(round).and(isRecent(date2, setRecentPeriod)));
		return prediction;
	}

	private Period getMatchRecentPeriod() {
		return Period.ofYears(config.getMatchRecentPeriod(getArea(), MATCH_RECENT_PERIOD_YEARS));
	}

	private Period getSetRecentPeriod() {
		return Period.ofYears(config.getSetRecentPeriod(getArea(), SET_RECENT_PERIOD_YEARS));
	}

	private void addItemProbabilities(MatchPrediction prediction, H2HPredictionItem item, Predicate<MatchData> filter) {
		addItemProbabilities(prediction, item, filter, filter);
	}

	private void addItemProbabilities(MatchPrediction prediction, H2HPredictionItem item, Predicate<MatchData> filter1, Predicate<MatchData> filter2) {
		var itemWeight = config.getItemWeight(item);
		if (itemWeight > 0.0) {
			ToIntFunction<MatchData> dimension = item.isForSet() ? MatchData::getPSets : MatchData::getPMatches;
			var won1 = matchData1.stream().filter(filter1.and(isOpponent(playerId2))).mapToInt(dimension).sum();
			var won2 = matchData2.stream().filter(filter2.and(isOpponent(playerId1))).mapToInt(dimension).sum();
			var total = won1 + won2;
			if (total > 0) {
				var weight = itemWeight * weight(total);
				var probabilityTransformer = probabilityTransformer(item.isForSet(), item.isMixedBestOf(), bestOf);
				prediction.addItemProbability1(item, weight, probabilityTransformer.applyAsDouble(1.0 * won1 / total));
				prediction.addItemProbability2(item, weight, probabilityTransformer.applyAsDouble(1.0 * won2 / total));
			}
		}
	}
}
