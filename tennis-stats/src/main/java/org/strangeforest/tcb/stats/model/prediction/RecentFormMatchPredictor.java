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
	private final PredictionConfig config;

	private static final int MATCH_RECENT_PERIOD_YEARS = 2;
	private static final int LAST_MATCHES_COUNT = 20;

	public RecentFormMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, RankingData rankingData1, RankingData rankingData2, PlayerData playerData1, PlayerData playerData2,
	                                LocalDate date1, LocalDate date2, Surface surface, TournamentLevel level, Round round, PredictionConfig config) {
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
		this.config = config;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.RECENT_FORM;
	}

	@Override public MatchPrediction predictMatch() {
		Period matchRecentPeriod = getMatchRecentPeriod();
		MatchPrediction prediction = new MatchPrediction(config.getTotalAreasWeight());
		int recentFormMatches = getRecentFormMatches();
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
		return config.getLastMatchesCount(getArea(), LAST_MATCHES_COUNT);
	}

	private void addItemProbabilities(MatchPrediction prediction, RecentFormPredictionItem item, Predicate<MatchData> filter1, Predicate<MatchData> filter2, Integer matches) {
		double itemWeight = config.getItemWeight(item);
		if (itemWeight > 0.0) {
			List<MatchData> filteredMatchData1 = matchData1.stream().filter(filter1).collect(toList());
			List<MatchData> filteredMatchData2 = matchData2.stream().filter(filter2).collect(toList());
			if (matches != null) {
				int matches1 = filteredMatchData1.size();
				int matches2 = filteredMatchData2.size();
				if (matches1 > matches)
					filteredMatchData1 = filteredMatchData1.subList(matches1 - matches, matches1);
				if (matches2 > matches)
					filteredMatchData2 = filteredMatchData2.subList(matches2 - matches, matches2);
			}
			int matches1 = filteredMatchData1.size();
			int matches2 = filteredMatchData2.size();
			if (matches1 > 0 && matches2 > 0) {
				double form1 = filteredMatchData1.stream().mapToDouble(MatchData::getOpponentEloScore).sum() / matches1;
				double form2 = filteredMatchData2.stream().mapToDouble(MatchData::getOpponentEloScore).sum() / matches2;
				double weight = itemWeight * weight(matches1, matches2);
				double p1 = winProbability(form1, form2);
				double p2 = winProbability(form2, form1);
				double p12 = p1 + p2;
				if (p12 > 0.0) {
					prediction.addItemProbability1(item, weight, p1 / p12);
					prediction.addItemProbability2(item, weight, p2 / p12);
				}
			}
		}
	}

	private static double winProbability(double form1, double form2) {
		return 1 / (1 + pow(10.0, form2 - form1));
	}
}
