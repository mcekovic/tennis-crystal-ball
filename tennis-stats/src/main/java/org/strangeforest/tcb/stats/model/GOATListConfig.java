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

	public static final GOATListConfig DEFAULT = new GOATListConfig(true, false, 1, 1, 1, emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
	public static final List<String> TOURNAMENT_LEVELS = asList("G", "F", "L", "M", "O", "A", "B", "D", "T");

	private final boolean oldLegends;
	private final boolean extrapolateCareer;
	private final int tournamentPointsFactor;
	private final int rankingPointsFactor;
	private final int achievementsPointsFactor;

	private final Map<String, Integer> levelPointsFactors;

	private final int yearEndRankPointsFactor;
	private final int bestRankPointsFactor;
	private final int weeksAtNo1PointsFactor;
	private final int weeksAtEloTopNPointsFactor;
	private final int bestEloRatingPointsFactor;

	private final int grandSlamPointsFactor;
	private final int bigWinsPointsFactor;
	private final int h2hPointsFactor;
	private final int recordsPointsFactor;
	private final int bestSeasonPointsFactor;
	private final int greatestRivalriesPointsFactor;
	private final int performancePointsFactor;
	private final int statisticsPointsFactor;

	public GOATListConfig(boolean oldLegends, boolean extrapolateCareer, int tournamentPointsFactor, int rankingPointsFactor, int achievementsPointsFactor, Map<String, Integer> levelPointsFactors, 
	                      int yearEndRankPointsFactor, int bestRankPointsFactor, int weeksAtNo1PointsFactor, int weeksAtEloTopNPointsFactor, int bestEloRatingPointsFactor,
	                      int grandSlamPointsFactor, int bigWinsPointsFactor, int h2hPointsFactor, int recordsPointsFactor, int bestSeasonPointsFactor, int greatestRivalriesPointsFactor, int performancePointsFactor, int statisticsPointsFactor) {
		this.oldLegends = oldLegends;
		this.extrapolateCareer = extrapolateCareer;
		this.tournamentPointsFactor = tournamentPointsFactor;
		this.rankingPointsFactor = rankingPointsFactor;
		this.achievementsPointsFactor = achievementsPointsFactor;
		this.levelPointsFactors = levelPointsFactors.entrySet().stream().filter(e -> e.getValue() != 1).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		this.yearEndRankPointsFactor = yearEndRankPointsFactor;
		this.bestRankPointsFactor = bestRankPointsFactor;
		this.weeksAtNo1PointsFactor = weeksAtNo1PointsFactor;
		this.weeksAtEloTopNPointsFactor = weeksAtEloTopNPointsFactor;
		this.bestEloRatingPointsFactor = bestEloRatingPointsFactor;
		this.grandSlamPointsFactor = grandSlamPointsFactor;
		this.bigWinsPointsFactor = bigWinsPointsFactor;
		this.h2hPointsFactor = h2hPointsFactor;
		this.recordsPointsFactor = recordsPointsFactor;
		this.bestSeasonPointsFactor = bestSeasonPointsFactor;
		this.greatestRivalriesPointsFactor = greatestRivalriesPointsFactor;
		this.performancePointsFactor = performancePointsFactor;
		this.statisticsPointsFactor = statisticsPointsFactor;
	}

	public boolean isOldLegends() {
		return oldLegends;
	}

	public boolean isExtrapolateCareer() {
		return extrapolateCareer;
	}

	public int getTournamentPointsFactor() {
		return tournamentPointsFactor;
	}

	public int getRankingPointsFactor() {
		return rankingPointsFactor;
	}

	public int getAchievementsPointsFactor() {
		return achievementsPointsFactor;
	}

	public int getLevelPointsFactor(String level) {
		return levelPointsFactors.getOrDefault(level, 1);
	}

	public int getYearEndRankPointsFactor() {
		return yearEndRankPointsFactor;
	}

	public int getBestRankPointsFactor() {
		return bestRankPointsFactor;
	}

	public int getWeeksAtNo1PointsFactor() {
		return weeksAtNo1PointsFactor;
	}

	public int getWeeksAtEloTopNPointsFactor() {
		return weeksAtEloTopNPointsFactor;
	}

	public int getBestEloRatingPointsFactor() {
		return bestEloRatingPointsFactor;
	}

	public int getGrandSlamPointsFactor() {
		return grandSlamPointsFactor;
	}

	public int getBigWinsPointsFactor() {
		return bigWinsPointsFactor;
	}

	public int getH2hPointsFactor() {
		return h2hPointsFactor;
	}

	public int getRecordsPointsFactor() {
		return recordsPointsFactor;
	}

	public int getBestSeasonPointsFactor() {
		return bestSeasonPointsFactor;
	}

	public int getGreatestRivalriesPointsFactor() {
		return greatestRivalriesPointsFactor;
	}

	public int getPerformancePointsFactor() {
		return performancePointsFactor;
	}

	public int getStatisticsPointsFactor() {
		return statisticsPointsFactor;
	}

	public int getLevelPointsTotalFactor(String level) {
		return tournamentPointsFactor * getLevelPointsFactor(level);
	}

	public int getYearEndRankPointsTotalFactor() {
		return rankingPointsFactor * yearEndRankPointsFactor;
	}

	public int getBestRankPointsTotalFactor() {
		return rankingPointsFactor * bestRankPointsFactor;
	}

	public int getWeeksAtNo1PointsTotalFactor() {
		return rankingPointsFactor * weeksAtNo1PointsFactor;
	}

	public int getWeeksAtEloTopNPointsTotalFactor() {
		return rankingPointsFactor * weeksAtEloTopNPointsFactor;
	}

	public int getBestEloRatingPointsTotalFactor() {
		return rankingPointsFactor * bestEloRatingPointsFactor;
	}

	public int getGrandSlamPointsTotalFactor() {
		return achievementsPointsFactor * grandSlamPointsFactor;
	}

	public int getBigWinsPointsTotalFactor() {
		return achievementsPointsFactor * bigWinsPointsFactor;
	}

	public int getH2hPointsTotalFactor() {
		return achievementsPointsFactor * h2hPointsFactor;
	}

	public int getRecordsPointsTotalFactor() {
		return achievementsPointsFactor * recordsPointsFactor;
	}

	public int getBestSeasonPointsTotalFactor() {
		return achievementsPointsFactor * bestSeasonPointsFactor;
	}

	public int getGreatestRivalriesPointsTotalFactor() {
		return achievementsPointsFactor * greatestRivalriesPointsFactor;
	}

	public int getPerformancePointsTotalFactor() {
		return achievementsPointsFactor * performancePointsFactor;
	}

	public int getStatisticsPointsTotalFactor() {
		return achievementsPointsFactor * statisticsPointsFactor;
	}

	public boolean hasDefaultFactors() {
		return hasDefaultFactors.get();
	}

	public boolean hasDefaultTournamentFactors() {
		return levelPointsFactors.isEmpty();
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
		return tournamentPointsFactor == 1 && rankingPointsFactor == 1 && achievementsPointsFactor == 1 && hasDefaultTournamentFactors() && hasDefaultRankingFactors() && hasDefaultAchievementsFactors();
	}

	private boolean checkHasDefaultRankingFactors() {
		return yearEndRankPointsFactor == 1 && bestRankPointsFactor == 1 && weeksAtNo1PointsFactor == 1 && weeksAtEloTopNPointsFactor == 1 && bestEloRatingPointsFactor == 1;
	}

	private boolean checkHasDefaultAchievementsFactors() {
		return grandSlamPointsFactor == 1 && bigWinsPointsFactor == 1 && h2hPointsFactor == 1 && recordsPointsFactor == 1 && bestSeasonPointsFactor == 1 && greatestRivalriesPointsFactor == 1 && performancePointsFactor == 1 && statisticsPointsFactor == 1;
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
			config.tournamentPointsFactor == tournamentPointsFactor &&
			config.rankingPointsFactor == rankingPointsFactor &&
			config.achievementsPointsFactor == achievementsPointsFactor &&
			config.levelPointsFactors.equals(levelPointsFactors) &&
			config.yearEndRankPointsFactor == yearEndRankPointsFactor &&
			config.bestRankPointsFactor == bestRankPointsFactor &&
			config.weeksAtNo1PointsFactor == weeksAtNo1PointsFactor &&
			config.weeksAtEloTopNPointsFactor == weeksAtEloTopNPointsFactor &&
			config.bestEloRatingPointsFactor == bestEloRatingPointsFactor &&
			config.grandSlamPointsFactor == grandSlamPointsFactor &&
			config.bigWinsPointsFactor == bigWinsPointsFactor &&
			config.h2hPointsFactor == h2hPointsFactor &&
			config.recordsPointsFactor == recordsPointsFactor &&
			config.bestSeasonPointsFactor == bestSeasonPointsFactor &&
			config.greatestRivalriesPointsFactor == greatestRivalriesPointsFactor &&
			config.performancePointsFactor == performancePointsFactor &&
			config.statisticsPointsFactor == statisticsPointsFactor;
	}

	@Override public int hashCode() {
		return Objects.hash(
			oldLegends, extrapolateCareer, tournamentPointsFactor, rankingPointsFactor, achievementsPointsFactor, levelPointsFactors,
			yearEndRankPointsFactor, bestRankPointsFactor, weeksAtNo1PointsFactor, weeksAtEloTopNPointsFactor, bestEloRatingPointsFactor,
			grandSlamPointsFactor, bigWinsPointsFactor, h2hPointsFactor, recordsPointsFactor, bestSeasonPointsFactor, greatestRivalriesPointsFactor, performancePointsFactor, statisticsPointsFactor
		);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("oldLegends", nullIf(oldLegends, false))
			.add("extrapolateCareer", nullIf(extrapolateCareer, true))
			.add("tournamentPointsFactor", nullIf(tournamentPointsFactor, 1))
			.add("rankingPointsFactor", nullIf(rankingPointsFactor, 1))
			.add("achievementsPointsFactor", nullIf(achievementsPointsFactor, 1))
			.add("levelPointsFactors", nullIf(levelPointsFactors, emptyMap()))
			.add("yearEndRankPointsFactor", nullIf(yearEndRankPointsFactor, 1))
			.add("bestRankPointsFactor", nullIf(bestRankPointsFactor, 1))
			.add("weeksAtNo1PointsFactor", nullIf(weeksAtNo1PointsFactor, 1))
			.add("weeksAtEloTopNPointsFactor", nullIf(weeksAtEloTopNPointsFactor, 1))
			.add("bestEloRatingPointsFactor", nullIf(bestEloRatingPointsFactor, 1))
			.add("grandSlamPointsFactor", nullIf(grandSlamPointsFactor, 1))
			.add("bigWinsPointsFactor", nullIf(bigWinsPointsFactor, 1))
			.add("h2hPointsFactor", nullIf(h2hPointsFactor, 1))
			.add("recordsPointsFactor", nullIf(recordsPointsFactor, 1))
			.add("bestSeasonPointsFactor", nullIf(bestSeasonPointsFactor, 1))
			.add("greatestRivalriesPointsFactor", nullIf(greatestRivalriesPointsFactor, 1))
			.add("performancePointsFactor", nullIf(performancePointsFactor, 1))
			.add("statisticsPointsFactor", nullIf(statisticsPointsFactor, 1))
			.toString();
	}
}
