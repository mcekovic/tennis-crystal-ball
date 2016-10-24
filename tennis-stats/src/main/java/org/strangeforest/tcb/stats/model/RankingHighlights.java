package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.util.*;

import static java.util.Comparator.*;

public class RankingHighlights {

	// Ranking
	private int currentRank;
	private int currentRankPoints;
	private int bestRank;
	private Date bestRankDate;
	private int bestRankPoints;
	private Date bestRankPointsDate;
	private int bestYearEndRank;
	private String bestYearEndRankSeasons;
	private int bestYearEndRankPoints;
	private String bestYearEndRankPointsSeasons;
	private int goatRank;
	private int goatPoints;
	private int bestEloRank;
	private Date bestEloRankDate;
	private int bestEloRating;
	private Date bestEloRatingDate;
	private int bestHardEloRank;
	private Date bestHardEloRankDate;
	private int bestHardEloRating;
	private Date bestHardEloRatingDate;
	private int bestClayEloRank;
	private Date bestClayEloRankDate;
	private int bestClayEloRating;
	private Date bestClayEloRatingDate;
	private int bestGrassEloRank;
	private Date bestGrassEloRankDate;
	private int bestGrassEloRating;
	private Date bestGrassEloRatingDate;
	private int bestCarpetEloRank;
	private Date bestCarpetEloRankDate;
	private int bestCarpetEloRating;
	private Date bestCarpetEloRatingDate;
	private Map<Integer, Double> weeksAt = new HashMap<>();
	private Map<Integer, Integer> yearEndsAt = new HashMap<>();

	// Weeks
	private double weeksAtNo1;
	private double weeksInTop5;
	private double weeksInTop10;
	private double weeksInTop20;
	private double weeksInTop50;
	private double weeksInTop100;

	// Year-ends
	private int yearEndsAtNo1;
	private int yearEndsInTop5;
	private int yearEndsInTop10;
	private int yearEndsInTop20;
	private int yearEndsInTop50;
	private int yearEndsInTop100;

	private static final int MAX_WEEKS = 52;


	// Ranking

	public int getCurrentRank() {
		return currentRank;
	}

	public void setCurrentRank(int currentRank) {
		this.currentRank = currentRank;
	}

	public int getCurrentRankPoints() {
		return currentRankPoints;
	}

	public void setCurrentRankPoints(int currentRankPoints) {
		this.currentRankPoints = currentRankPoints;
	}

	public int getBestRank() {
		return bestRank;
	}

	public void setBestRank(int bestRank) {
		this.bestRank = bestRank;
	}

	public Date getBestRankDate() {
		return bestRankDate;
	}

	public void setBestRankDate(Date bestRankDate) {
		this.bestRankDate = bestRankDate;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public Date getBestRankPointsDate() {
		return bestRankPointsDate;
	}

	public void setBestRankPointsDate(Date bestRankPointsDate) {
		this.bestRankPointsDate = bestRankPointsDate;
	}

	public int getBestYearEndRank() {
		return bestYearEndRank;
	}

	public void setBestYearEndRank(int bestYearEndRank) {
		this.bestYearEndRank = bestYearEndRank;
	}

	public String getBestYearEndRankSeasons() {
		return bestYearEndRankSeasons;
	}

	public void setBestYearEndRankSeasons(String bestYearEndRankSeasons) {
		this.bestYearEndRankSeasons = bestYearEndRankSeasons;
	}

	public int getBestYearEndRankPoints() {
		return bestYearEndRankPoints;
	}

	public void setBestYearEndRankPoints(int bestYearEndRankPoints) {
		this.bestYearEndRankPoints = bestYearEndRankPoints;
	}

	public String getBestYearEndRankPointsSeasons() {
		return bestYearEndRankPointsSeasons;
	}

	public void setBestYearEndRankPointsSeasons(String bestYearEndRankPointsSeasons) {
		this.bestYearEndRankPointsSeasons = bestYearEndRankPointsSeasons;
	}

	public int getGoatRank() {
		return goatRank;
	}

	public void setGoatRank(int goatRank) {
		this.goatRank = goatRank;
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public void setGoatPoints(int goatPoints) {
		this.goatPoints = goatPoints;
	}

	public int getBestEloRank() {
		return bestEloRank;
	}

	public void setBestEloRank(int bestEloRank) {
		this.bestEloRank = bestEloRank;
	}

	public Date getBestEloRankDate() {
		return bestEloRankDate;
	}

	public void setBestEloRankDate(Date bestEloRankDate) {
		this.bestEloRankDate = bestEloRankDate;
	}

	public int getBestEloRating() {
		return bestEloRating;
	}

	public void setBestEloRating(int bestEloRating) {
		this.bestEloRating = bestEloRating;
	}

	public Date getBestEloRatingDate() {
		return bestEloRatingDate;
	}

	public void setBestEloRatingDate(Date bestEloRatingDate) {
		this.bestEloRatingDate = bestEloRatingDate;
	}

	public int getBestHardEloRank() {
		return bestHardEloRank;
	}

	public void setBestHardEloRank(int bestHardEloRank) {
		this.bestHardEloRank = bestHardEloRank;
	}

	public Date getBestHardEloRankDate() {
		return bestHardEloRankDate;
	}

	public void setBestHardEloRankDate(Date bestHardEloRankDate) {
		this.bestHardEloRankDate = bestHardEloRankDate;
	}

	public int getBestHardEloRating() {
		return bestHardEloRating;
	}

	public void setBestHardEloRating(int bestHardEloRating) {
		this.bestHardEloRating = bestHardEloRating;
	}

	public Date getBestHardEloRatingDate() {
		return bestHardEloRatingDate;
	}

	public void setBestHardEloRatingDate(Date bestHardEloRatingDate) {
		this.bestHardEloRatingDate = bestHardEloRatingDate;
	}

	public int getBestClayEloRank() {
		return bestClayEloRank;
	}

	public void setBestClayEloRank(int bestClayEloRank) {
		this.bestClayEloRank = bestClayEloRank;
	}

	public Date getBestClayEloRankDate() {
		return bestClayEloRankDate;
	}

	public void setBestClayEloRankDate(Date bestClayEloRankDate) {
		this.bestClayEloRankDate = bestClayEloRankDate;
	}

	public int getBestClayEloRating() {
		return bestClayEloRating;
	}

	public void setBestClayEloRating(int bestClayEloRating) {
		this.bestClayEloRating = bestClayEloRating;
	}

	public Date getBestClayEloRatingDate() {
		return bestClayEloRatingDate;
	}

	public void setBestClayEloRatingDate(Date bestClayEloRatingDate) {
		this.bestClayEloRatingDate = bestClayEloRatingDate;
	}

	public int getBestGrassEloRank() {
		return bestGrassEloRank;
	}

	public void setBestGrassEloRank(int bestGrassEloRank) {
		this.bestGrassEloRank = bestGrassEloRank;
	}

	public Date getBestGrassEloRankDate() {
		return bestGrassEloRankDate;
	}

	public void setBestGrassEloRankDate(Date bestGrassEloRankDate) {
		this.bestGrassEloRankDate = bestGrassEloRankDate;
	}

	public int getBestGrassEloRating() {
		return bestGrassEloRating;
	}

	public void setBestGrassEloRating(int bestGrassEloRating) {
		this.bestGrassEloRating = bestGrassEloRating;
	}

	public Date getBestGrassEloRatingDate() {
		return bestGrassEloRatingDate;
	}

	public void setBestGrassEloRatingDate(Date bestGrassEloRatingDate) {
		this.bestGrassEloRatingDate = bestGrassEloRatingDate;
	}

	public int getBestCarpetEloRank() {
		return bestCarpetEloRank;
	}

	public void setBestCarpetEloRank(int bestCarpetEloRank) {
		this.bestCarpetEloRank = bestCarpetEloRank;
	}

	public Date getBestCarpetEloRankDate() {
		return bestCarpetEloRankDate;
	}

	public void setBestCarpetEloRankDate(Date bestCarpetEloRankDate) {
		this.bestCarpetEloRankDate = bestCarpetEloRankDate;
	}

	public int getBestCarpetEloRating() {
		return bestCarpetEloRating;
	}

	public void setBestCarpetEloRating(int bestCarpetEloRating) {
		this.bestCarpetEloRating = bestCarpetEloRating;
	}

	public Date getBestCarpetEloRatingDate() {
		return bestCarpetEloRatingDate;
	}

	public void setBestCarpetEloRatingDate(Date bestCarpetEloRatingDate) {
		this.bestCarpetEloRatingDate = bestCarpetEloRatingDate;
	}

	private Supplier<FrequentRank<Double>> mostFrequentRank = Memoizer.of(() -> findMostFrequentRank(weeksAt));

	public FrequentRank<Double> getMostFrequentRank() {
		return mostFrequentRank.get();
	}

	private Supplier<FrequentRank<Integer>> mostFrequentYearEndRank = Memoizer.of(() -> findMostFrequentRank(yearEndsAt));

	public FrequentRank<Integer> getMostFrequentYearEndRank() {
		return mostFrequentYearEndRank.get();
	}

	private static <T extends Comparable<T>> FrequentRank<T> findMostFrequentRank(Map<Integer, T> at) {
		return at.entrySet().stream().map(entry -> new FrequentRank<>(entry.getKey(), entry.getValue())).max(naturalOrder()).orElse(null);
	}


	// Weeks

	public int getWeeksAtNo1() {
		return (int)Math.ceil(weeksAtNo1);
	}

	public int getWeeksInTop5() {
		return (int)Math.ceil(weeksInTop5);
	}

	public int getWeeksInTop10() {
		return (int)Math.ceil(weeksInTop10);
	}

	public int getWeeksInTop20() {
		return (int)Math.ceil(weeksInTop20);
	}

	public int getWeeksInTop50() {
		return (int)Math.ceil(weeksInTop50);
	}

	public int getWeeksInTop100() {
		return (int)Math.ceil(weeksInTop100);
	}

	public void processWeeksAt(int rank, double weeks) {
		if (rank == 1)
			weeksAtNo1 += weeks;
		if (rank <= 5)
			weeksInTop5 += weeks;
		if (rank <= 10)
			weeksInTop10 += weeks;
		if (rank <= 20)
			weeksInTop20 += weeks;
		if (rank <= 50)
			weeksInTop50 += weeks;
		if (rank <= 100)
			weeksInTop100 += weeks;
		Double w = weeksAt.get(rank);
		weeksAt.put(rank, w != null ? w + weeks : weeks);
	}


	// Year-ends

	public int getYearEndsAtNo1() {
		return yearEndsAtNo1;
	}

	public int getYearEndsInTop5() {
		return yearEndsInTop5;
	}

	public int getYearEndsInTop10() {
		return yearEndsInTop10;
	}

	public int getYearEndsInTop20() {
		return yearEndsInTop20;
	}

	public int getYearEndsInTop50() {
		return yearEndsInTop50;
	}

	public int getYearEndsInTop100() {
		return yearEndsInTop100;
	}

	public void processYearEndRank(int rank) {
		if (rank == 1)
			++yearEndsAtNo1;
		if (rank <= 5)
			++yearEndsInTop5;
		if (rank <= 10)
			++yearEndsInTop10;
		if (rank <= 20)
			++yearEndsInTop20;
		if (rank <= 50)
			++yearEndsInTop50;
		if (rank <= 100)
			++yearEndsInTop100;
		Integer s = yearEndsAt.get(rank);
		yearEndsAt.put(rank, s != null ? s + 1 : 1);
	}


	public static class FrequentRank<T extends Comparable<T>> implements Comparable<FrequentRank<T>> {

		private final int rank;
		private final T duration;

		public FrequentRank(int rank, T duration) {
			this.rank = rank;
			this.duration = duration;
		}

		public int getRank() {
			return rank;
		}

		public T getDuration() {
			return duration;
		}

		@Override public int compareTo(FrequentRank<T> frequentRank) {
			int result = duration.compareTo(frequentRank.duration);
			return result != 0 ? result : -Integer.compare(rank, frequentRank.rank);
		}
	}
}
