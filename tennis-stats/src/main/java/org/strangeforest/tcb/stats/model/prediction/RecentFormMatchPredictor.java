package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.stats.model.prediction.RecentFormPredictionItem.*;

public class RecentFormMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final Range<Integer> rankRange1;
	private final Range<Integer> rankRange2;
	private final PlayerData playerData1;
	private final PlayerData playerData2;
	private final LocalDate date1;
	private final LocalDate date2;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final short bestOf;
	private final PredictionConfig config;

	private static final int MATCH_RECENT_PERIOD_YEARS = 2;
	private static final int LAST_MATCHES_COUNT = 30;

	public RecentFormMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, RankingData rankingData1, RankingData rankingData2, PlayerData playerData1, PlayerData playerData2,
	                                LocalDate date1, LocalDate date2, Surface surface, TournamentLevel level, Round round, short bestOf, PredictionConfig config) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.rankRange1 = rankRange(rankingData1.getRank());
		this.rankRange2 = rankRange(rankingData2.getRank());
		this.playerData1 = playerData1;
		this.playerData2 = playerData2;
		this.date1 = date1;
		this.date2 = date2;
		this.surface = surface;
		this.level = level;
		this.round = round;
		this.bestOf = bestOf;
		this.config = config;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.RECENT_FORM;
	}

	@Override public MatchPrediction predictMatch() {
		var matchRecentPeriod = getMatchRecentPeriod();
		var prediction = new MatchPrediction(config.getTotalAreasWeight(), bestOf);
		var recentFormMatches = getRecentFormMatches();
		addItemProbabilities(prediction, OVERALL, isRecent(date1, matchRecentPeriod), isRecent(date2, matchRecentPeriod), recentFormMatches);
		addItemProbabilities(prediction, SURFACE, isSurface(surface).and(isRecent(date1, matchRecentPeriod)), isSurface(surface).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		addItemProbabilities(prediction, LEVEL, isLevel(level).and(isRecent(date1, matchRecentPeriod)), isLevel(level).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		addItemProbabilities(prediction, ROUND, isRound(round).and(isRecent(date1, matchRecentPeriod)), isRound(round).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		addItemProbabilities(prediction, VS_RANK, isOpponentRankInRange(rankRange2).and(isRecent(date1, matchRecentPeriod)), isOpponentRankInRange(rankRange1).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		addItemProbabilities(prediction, VS_HAND, isOpponentHand(playerData2.getHand()).and(isRecent(date1, matchRecentPeriod)), isOpponentHand(playerData1.getHand()).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		addItemProbabilities(prediction, VS_BACKHAND, isOpponentBackhand(playerData2.getBackhand()).and(isRecent(date1, matchRecentPeriod)), isOpponentBackhand(playerData1.getBackhand()).and(isRecent(date2, matchRecentPeriod)), recentFormMatches);
		return prediction;
	}

	private Period getMatchRecentPeriod() {
		return Period.ofYears(config.getMatchRecentPeriod(getArea(), MATCH_RECENT_PERIOD_YEARS));
	}

	private int getRecentFormMatches() {
		return config.getLastMatchesCount(getArea(), 3 * LAST_MATCHES_COUNT / 2);
	}

	private void addItemProbabilities(MatchPrediction prediction, RecentFormPredictionItem item, Predicate<MatchData> filter1, Predicate<MatchData> filter2, Integer matches) {
		var itemWeight = config.getItemWeight(item);
		if (itemWeight > 0.0) {
			var filteredMatchData1 = matchData1.stream().filter(filter1).collect(toList());
			var filteredMatchData2 = matchData2.stream().filter(filter2).collect(toList());
			if (matches != null) {
				var matches1 = filteredMatchData1.size();
				var matches2 = filteredMatchData2.size();
				if (matches1 > matches)
					filteredMatchData1 = filteredMatchData1.subList(matches1 - matches, matches1);
				if (matches2 > matches)
					filteredMatchData2 = filteredMatchData2.subList(matches2 - matches, matches2);
			}
			var matches1 = filteredMatchData1.size();
			var matches2 = filteredMatchData2.size();
			if (matches1 > 0 && matches2 > 0) {
				var form1 = 0.0;
				for (var i = 0; i < matches1; i++)
					form1 += filteredMatchData1.get(i).getOpponentEloScore() / recencyAdjustment(matches1 - i, LAST_MATCHES_COUNT);
				form1 /= matches1;
				var form2 = 0.0;
				for (var i = 0; i < matches2; i++)
					form2 += filteredMatchData2.get(i).getOpponentEloScore() / recencyAdjustment(matches2 - i, LAST_MATCHES_COUNT);
				form2 /= matches2;
				var weight = itemWeight * weight(matches1, matches2);
				var p1 = winProbability(form1, form2);
				var p2 = winProbability(form2, form1);
				var p12 = p1 + p2;
				if (p12 > 0.0) {
					prediction.addItemProbability1(item, weight, matchProbabilityFromMixedProbability(p1 / p12, bestOf));
					prediction.addItemProbability2(item, weight, matchProbabilityFromMixedProbability(p2 / p12, bestOf));
				}
			}
		}
	}

	private static double winProbability(double form1, double form2) {
		return 1 / (1 + pow(10.0, (form2 - form1) / 350.0));
	}
}
