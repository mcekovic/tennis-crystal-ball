package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import com.google.common.collect.*;

import static java.util.Arrays.asList;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.stats.model.prediction.WinningPctPredictionItem.*;

public class WinningPctMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final Range<Integer> rankRange1;
	private final Range<Integer> rankRange2;
	private final PlayerData playerData1;
	private final PlayerData playerData2;
	private final Date date;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final short bestOf;

	private static final int RECENT_PERIOD_YEARS = 2;

	public WinningPctMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, RankingData rankingData1, RankingData rankingData2, PlayerData playerData1, PlayerData playerData2,
	                                Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.rankRange1 = rankRange(rankingData1.getRank());
		this.rankRange2 = rankRange(rankingData2.getRank());
		this.playerData1 = playerData1;
		this.playerData2 = playerData2;
		this.date = date;
		this.surface = surface;
		this.level = level;
		this.round = round;
		this.bestOf = bestOf;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.WINNING_PCT;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, MATCH, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface));
		addItemProbabilities(prediction, LEVEL, isLevel(level));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date, getRecentPeriod()));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, VS_RANK, isOpponentRankInRange(rankRange2), isOpponentRankInRange(rankRange1));
		addItemProbabilities(prediction, VS_HAND, isOpponentHand(playerData2.getHand()), isOpponentHand(playerData1.getHand()));
		addItemProbabilities(prediction, VS_BACKHAND, isOpponentBackhand(playerData2.getBackhand()), isOpponentBackhand(playerData1.getBackhand()));
		addItemProbabilities(prediction, SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, ROUND_SET, isRound(round));
		addItemProbabilities(prediction, RECENT_SET, isRecent(date, getRecentPeriod()));
		addItemProbabilities(prediction, SURFACE_RECENT_SET, isSurface(surface).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, LEVEL_RECENT_SET, isLevel(level).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, ROUND_RECENT_SET, isRound(round).and(isRecent(date, getRecentPeriod())));
		addItemProbabilities(prediction, VS_RANK_SET, isOpponentRankInRange(rankRange2), isOpponentRankInRange(rankRange1));
		addItemProbabilities(prediction, VS_HAND_SET, isOpponentHand(playerData2.getHand()), isOpponentHand(playerData1.getHand()));
		addItemProbabilities(prediction, VS_BACKHAND_SET, isOpponentBackhand(playerData2.getBackhand()), isOpponentBackhand(playerData1.getBackhand()));
		return prediction;
	}

	private Period getRecentPeriod() {
		return Period.ofYears(PredictionConfig.getIntegerProperty("recent_period." + getArea()).orElse(RECENT_PERIOD_YEARS));
	}

	private void addItemProbabilities(MatchPrediction prediction, WinningPctPredictionItem item, Predicate<MatchData> filter) {
		addItemProbabilities(prediction, item, filter, filter);
	}

	private void addItemProbabilities(MatchPrediction prediction, WinningPctPredictionItem item, Predicate<MatchData> filter1, Predicate<MatchData> filter2) {
		if (item.getWeight() > 0.0) {
			ToIntFunction<MatchData> wonDimension = item.isForSet() ? MatchData::getPSets : MatchData::getPMatches;
			ToIntFunction<MatchData> lostDimension = item.isForSet() ? MatchData::getOSets : MatchData::getOMatches;
			long won1 = matchData1.stream().filter(filter1).mapToInt(wonDimension).sum();
			long lost1 = matchData1.stream().filter(filter1).mapToInt(lostDimension).sum();
			long won2 = matchData2.stream().filter(filter2).mapToInt(wonDimension).sum();
			long lost2 = matchData2.stream().filter(filter2).mapToInt(lostDimension).sum();
			long total1 = won1 + lost1;
			long total2 = won2 + lost2;
			if (total1 > 0 && total2 > 0) {
				double weight = item.getWeight() * weight(total1, total2);
				double p1 = 1.0 * won1 / total1;
				double p2 = 1.0 * won2 / total2;
				double p12 = p1 + p2;
				if (p12 > 0.0) {
					DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item.isForSet(), bestOf);
					prediction.addItemProbability1(getArea(), item, weight, probabilityTransformer.applyAsDouble(p1 / p12));
					prediction.addItemProbability2(getArea(), item, weight, probabilityTransformer.applyAsDouble(p2 / p12));
				}
			}
		}
	}

	private static final List<Range<Integer>> RANK_RANGES = asList(
		Range.closed(1, 5),
		Range.openClosed(5, 10),
		Range.openClosed(10, 20),
		Range.openClosed(20, 50),
		Range.openClosed(50, 100),
		Range.greaterThan(100)
	);

	private static Range<Integer> rankRange(Integer rank) {
		return rank != null ? RANK_RANGES.stream().filter(range -> range.contains(rank)).findFirst().orElse(null) : null;
	}
}
