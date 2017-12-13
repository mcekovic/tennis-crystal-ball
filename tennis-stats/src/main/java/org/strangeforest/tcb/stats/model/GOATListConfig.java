package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.Objects;
import java.util.function.Supplier;

import org.strangeforest.tcb.util.*;

import com.google.common.base.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class GOATListConfig {

	public static final GOATListConfig DEFAULT = new GOATListConfig(true, false, 1, 1, 1, emptyMap(), emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
	public static final List<String> TOURNAMENT_LEVELS = asList("G", "F", "L", "M", "O", "A", "B", "D", "T");
	public static final List<String> TOURNAMENT_RESULTS = asList("W", "F", "SF", "QF", "RR", "BR");

	private final boolean oldLegends;
	private final boolean extrapolateCareer;
	private final int tournamentFactor;
	private final int rankingFactor;
	private final int achievementsFactor;

	private final Map<String, Integer> levelFactors;
	private final Map<String, Integer> resultFactors;

	private final int yearEndRankFactor;
	private final int bestRankFactor;
	private final int weeksAtNo1Factor;
	private final int weeksAtEloTopNFactor;
	private final int bestEloRatingFactor;

	private final int grandSlamFactor;
	private final int bigWinsFactor;
	private final int h2hFactor;
	private final int recordsFactor;
	private final int bestSeasonFactor;
	private final int greatestRivalriesFactor;
	private final int performanceFactor;
	private final int statisticsFactor;

	public GOATListConfig(boolean oldLegends, boolean extrapolateCareer, int tournamentFactor, int rankingFactor, int achievementsFactor, Map<String, Integer> levelFactors, Map<String, Integer> resultFactors,
	                      int yearEndRankFactor, int bestRankFactor, int weeksAtNo1Factor, int weeksAtEloTopNFactor, int bestEloRatingFactor,
	                      int grandSlamFactor, int bigWinsFactor, int h2hFactor, int recordsFactor, int bestSeasonFactor, int greatestRivalriesFactor, int performanceFactor, int statisticsFactor) {
		this.oldLegends = oldLegends;
		this.extrapolateCareer = extrapolateCareer;
		this.tournamentFactor = tournamentFactor;
		this.rankingFactor = rankingFactor;
		this.achievementsFactor = achievementsFactor;
		this.levelFactors = filterOutDefaults(levelFactors);
		this.resultFactors = filterOutDefaults(resultFactors);
		this.yearEndRankFactor = yearEndRankFactor;
		this.bestRankFactor = bestRankFactor;
		this.weeksAtNo1Factor = weeksAtNo1Factor;
		this.weeksAtEloTopNFactor = weeksAtEloTopNFactor;
		this.bestEloRatingFactor = bestEloRatingFactor;
		this.grandSlamFactor = grandSlamFactor;
		this.bigWinsFactor = bigWinsFactor;
		this.h2hFactor = h2hFactor;
		this.recordsFactor = recordsFactor;
		this.bestSeasonFactor = bestSeasonFactor;
		this.greatestRivalriesFactor = greatestRivalriesFactor;
		this.performanceFactor = performanceFactor;
		this.statisticsFactor = statisticsFactor;
	}

	private static Map<String, Integer> filterOutDefaults(Map<String, Integer> factorMap) {
		return factorMap.entrySet().stream().filter(e -> e.getValue() != 1).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public boolean isOldLegends() {
		return oldLegends;
	}

	public boolean isExtrapolateCareer() {
		return extrapolateCareer;
	}

	public int getTournamentFactor() {
		return tournamentFactor;
	}

	public int getRankingFactor() {
		return rankingFactor;
	}

	public int getAchievementsFactor() {
		return achievementsFactor;
	}

	public int getLevelFactor(String level) {
		return levelFactors.getOrDefault(level, 1);
	}

	public int getResultFactor(String result) {
		return resultFactors.getOrDefault(result, 1);
	}

	public int getYearEndRankFactor() {
		return yearEndRankFactor;
	}

	public int getBestRankFactor() {
		return bestRankFactor;
	}

	public int getWeeksAtNo1Factor() {
		return weeksAtNo1Factor;
	}

	public int getWeeksAtEloTopNFactor() {
		return weeksAtEloTopNFactor;
	}

	public int getBestEloRatingFactor() {
		return bestEloRatingFactor;
	}

	public int getGrandSlamFactor() {
		return grandSlamFactor;
	}

	public int getBigWinsFactor() {
		return bigWinsFactor;
	}

	public int getH2hFactor() {
		return h2hFactor;
	}

	public int getRecordsFactor() {
		return recordsFactor;
	}

	public int getBestSeasonFactor() {
		return bestSeasonFactor;
	}

	public int getGreatestRivalriesFactor() {
		return greatestRivalriesFactor;
	}

	public int getPerformanceFactor() {
		return performanceFactor;
	}

	public int getStatisticsFactor() {
		return statisticsFactor;
	}

	public int getLevelTotalFactor(String level) {
		return tournamentFactor * getLevelFactor(level);
	}

	public int getResultTotalFactor(String result) {
		return tournamentFactor * getResultFactor(result);
	}

	public int getYearEndRankTotalFactor() {
		return rankingFactor * yearEndRankFactor;
	}

	public int getBestRankTotalFactor() {
		return rankingFactor * bestRankFactor;
	}

	public int getWeeksAtNo1TotalFactor() {
		return rankingFactor * weeksAtNo1Factor;
	}

	public int getWeeksAtEloTopNTotalFactor() {
		return rankingFactor * weeksAtEloTopNFactor;
	}

	public int getBestEloRatingTotalFactor() {
		return rankingFactor * bestEloRatingFactor;
	}

	public int getGrandSlamTotalFactor() {
		return achievementsFactor * grandSlamFactor;
	}

	public int getBigWinsTotalFactor() {
		return achievementsFactor * bigWinsFactor;
	}

	public int getH2hTotalFactor() {
		return achievementsFactor * h2hFactor;
	}

	public int getRecordsTotalFactor() {
		return achievementsFactor * recordsFactor;
	}

	public int getBestSeasonTotalFactor() {
		return achievementsFactor * bestSeasonFactor;
	}

	public int getGreatestRivalriesTotalFactor() {
		return achievementsFactor * greatestRivalriesFactor;
	}

	public int getPerformanceTotalFactor() {
		return achievementsFactor * performanceFactor;
	}

	public int getStatisticsTotalFactor() {
		return achievementsFactor * statisticsFactor;
	}

	public boolean hasDefaultFactors() {
		return hasDefaultFactors.get();
	}

	public boolean hasDefaultTournamentFactors() {
		return levelFactors.isEmpty() && resultFactors.isEmpty();
	}

	public boolean hasDefaultRankingFactors() {
		return hasDefaultRankingFactors.get();
	}

	public boolean hasDefaultAchievementsFactors() {
		return hasDefaultAchievementsFactors.get();
	}

	private Supplier<Boolean> hasDefaultFactors = Memoizer.of(this::checkHasDefaultFactors);
	private Supplier<Boolean> hasDefaultRankingFactors = Memoizer.of(this::checkHasDefaultRankingFactors);
	private Supplier<Boolean> hasDefaultAchievementsFactors = Memoizer.of(this::checkHasDefaultAchievementsFactors);

	private boolean checkHasDefaultFactors() {
		return tournamentFactor == 1 && rankingFactor == 1 && achievementsFactor == 1 && hasDefaultTournamentFactors() && hasDefaultRankingFactors() && hasDefaultAchievementsFactors();
	}

	private boolean checkHasDefaultRankingFactors() {
		return yearEndRankFactor == 1 && bestRankFactor == 1 && weeksAtNo1Factor == 1 && weeksAtEloTopNFactor == 1 && bestEloRatingFactor == 1;
	}

	private boolean checkHasDefaultAchievementsFactors() {
		return grandSlamFactor == 1 && bigWinsFactor == 1 && h2hFactor == 1 && recordsFactor == 1 && bestSeasonFactor == 1 && greatestRivalriesFactor == 1 && performanceFactor == 1 && statisticsFactor == 1;
	}

	public boolean isDefault() {
		return equals(DEFAULT);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GOATListConfig config = (GOATListConfig)o;
		return config.oldLegends == oldLegends && config.extrapolateCareer == extrapolateCareer &&
			config.tournamentFactor == tournamentFactor &&
			config.rankingFactor == rankingFactor &&
			config.achievementsFactor == achievementsFactor &&
			config.levelFactors.equals(levelFactors) &&
			config.resultFactors.equals(resultFactors) &&
			config.yearEndRankFactor == yearEndRankFactor &&
			config.bestRankFactor == bestRankFactor &&
			config.weeksAtNo1Factor == weeksAtNo1Factor &&
			config.weeksAtEloTopNFactor == weeksAtEloTopNFactor &&
			config.bestEloRatingFactor == bestEloRatingFactor &&
			config.grandSlamFactor == grandSlamFactor &&
			config.bigWinsFactor == bigWinsFactor &&
			config.h2hFactor == h2hFactor &&
			config.recordsFactor == recordsFactor &&
			config.bestSeasonFactor == bestSeasonFactor &&
			config.greatestRivalriesFactor == greatestRivalriesFactor &&
			config.performanceFactor == performanceFactor &&
			config.statisticsFactor == statisticsFactor;
	}

	@Override public int hashCode() {
		return Objects.hash(
			oldLegends, extrapolateCareer, tournamentFactor, rankingFactor, achievementsFactor, levelFactors, resultFactors,
			yearEndRankFactor, bestRankFactor, weeksAtNo1Factor, weeksAtEloTopNFactor, bestEloRatingFactor,
			grandSlamFactor, bigWinsFactor, h2hFactor, recordsFactor, bestSeasonFactor, greatestRivalriesFactor, performanceFactor, statisticsFactor
		);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("oldLegends", nullIf(oldLegends, false))
			.add("extrapolateCareer", nullIf(extrapolateCareer, true))
			.add("tournamentFactor", nullIf(tournamentFactor, 1))
			.add("rankingFactor", nullIf(rankingFactor, 1))
			.add("achievementsFactor", nullIf(achievementsFactor, 1))
			.add("levelFactors", nullIf(levelFactors, emptyMap()))
			.add("resultFactors", nullIf(resultFactors, emptyMap()))
			.add("yearEndRankFactor", nullIf(yearEndRankFactor, 1))
			.add("bestRankFactor", nullIf(bestRankFactor, 1))
			.add("weeksAtNo1Factor", nullIf(weeksAtNo1Factor, 1))
			.add("weeksAtEloTopNFactor", nullIf(weeksAtEloTopNFactor, 1))
			.add("bestEloRatingFactor", nullIf(bestEloRatingFactor, 1))
			.add("grandSlamFactor", nullIf(grandSlamFactor, 1))
			.add("bigWinsFactor", nullIf(bigWinsFactor, 1))
			.add("h2hFactor", nullIf(h2hFactor, 1))
			.add("recordsFactor", nullIf(recordsFactor, 1))
			.add("bestSeasonFactor", nullIf(bestSeasonFactor, 1))
			.add("greatestRivalriesFactor", nullIf(greatestRivalriesFactor, 1))
			.add("performanceFactor", nullIf(performanceFactor, 1))
			.add("statisticsFactor", nullIf(statisticsFactor, 1))
			.toString();
	}
}
