package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.util.*;

import static java.util.Comparator.*;

public class RankingHighlights {

	// Ranking
	private int currentRank;
	private int currentRankPoints;
	private int bestRank;
	private LocalDate bestRankDate;
	private int bestRankPoints;
	private LocalDate bestRankPointsDate;
	private int bestYearEndRank;
	private String bestYearEndRankSeasons;
	private int bestYearEndRankPoints;
	private String bestYearEndRankPointsSeasons;
	private int goatRank;
	private int goatPoints;
	private EloHighlights elo;
	private EloHighlights recentElo;
	private EloHighlights hardElo;
	private EloHighlights clayElo;
	private EloHighlights grassElo;
	private EloHighlights carpetElo;
	private EloHighlights outdoorElo;
	private EloHighlights indoorElo;
	private EloHighlights setElo;
	private EloHighlights gameElo;
	private EloHighlights serviceGameElo;
	private EloHighlights returnGameElo;
	private EloHighlights tieBreakElo;
	private final Map<Integer, Double> weeksAt = new HashMap<>();
	private final Map<Integer, Integer> yearEndsAt = new HashMap<>();

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

	public LocalDate getBestRankDate() {
		return bestRankDate;
	}

	public void setBestRankDate(LocalDate bestRankDate) {
		this.bestRankDate = bestRankDate;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public LocalDate getBestRankPointsDate() {
		return bestRankPointsDate;
	}

	public void setBestRankPointsDate(LocalDate bestRankPointsDate) {
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

	public EloHighlights getElo() {
		return elo;
	}

	public void setElo(EloHighlights elo) {
		this.elo = elo;
	}

	public EloHighlights getRecentElo() {
		return recentElo;
	}

	public void setRecentElo(EloHighlights recentElo) {
		this.recentElo = recentElo;
	}

	public EloHighlights getHardElo() {
		return hardElo;
	}

	public void setHardElo(EloHighlights hardElo) {
		this.hardElo = hardElo;
	}

	public EloHighlights getClayElo() {
		return clayElo;
	}

	public void setClayElo(EloHighlights clayElo) {
		this.clayElo = clayElo;
	}

	public EloHighlights getGrassElo() {
		return grassElo;
	}

	public void setGrassElo(EloHighlights grassElo) {
		this.grassElo = grassElo;
	}

	public EloHighlights getCarpetElo() {
		return carpetElo;
	}

	public void setCarpetElo(EloHighlights carpetElo) {
		this.carpetElo = carpetElo;
	}

	public EloHighlights getOutdoorElo() {
		return outdoorElo;
	}

	public void setOutdoorElo(EloHighlights outdoorElo) {
		this.outdoorElo = outdoorElo;
	}

	public EloHighlights getIndoorElo() {
		return indoorElo;
	}

	public void setIndoorElo(EloHighlights indoorElo) {
		this.indoorElo = indoorElo;
	}

	public EloHighlights getSetElo() {
		return setElo;
	}

	public void setSetElo(EloHighlights setElo) {
		this.setElo = setElo;
	}

	public EloHighlights getGameElo() {
		return gameElo;
	}

	public void setGameElo(EloHighlights gameElo) {
		this.gameElo = gameElo;
	}

	public EloHighlights getServiceGameElo() {
		return serviceGameElo;
	}

	public void setServiceGameElo(EloHighlights serviceGameElo) {
		this.serviceGameElo = serviceGameElo;
	}

	public EloHighlights getReturnGameElo() {
		return returnGameElo;
	}

	public void setReturnGameElo(EloHighlights returnGameElo) {
		this.returnGameElo = returnGameElo;
	}

	public EloHighlights getTieBreakElo() {
		return tieBreakElo;
	}

	public void setTieBreakElo(EloHighlights tieBreakElo) {
		this.tieBreakElo = tieBreakElo;
	}

	public boolean hasElo() {
		return !(elo.isEmpty() && recentElo.isEmpty() && hardElo.isEmpty() && clayElo.isEmpty() && grassElo.isEmpty() && carpetElo.isEmpty() && outdoorElo.isEmpty() && indoorElo.isEmpty()
			&& setElo.isEmpty() && gameElo.isEmpty() && serviceGameElo.isEmpty() && returnGameElo.isEmpty() && tieBreakElo.isEmpty());
	}

	public boolean hasCurrentElo() {
		return elo.currentRank > 0 || recentElo.currentRank > 0 || hardElo.currentRank > 0 || clayElo.currentRank > 0 || grassElo.currentRank > 0 || carpetElo.currentRank > 0 || outdoorElo.currentRank > 0 || indoorElo.currentRank > 0
			|| setElo.currentRank > 0 || gameElo.currentRank > 0 || serviceGameElo.currentRank > 0 || returnGameElo.currentRank > 0 || tieBreakElo.currentRank > 0;
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
		if (weeks > 54.0)
			return;
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
		weeksAt.compute(rank, (r, w) -> w != null ? w + weeks : weeks);
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
		yearEndsAt.compute(rank, (r, ye) -> ye != null ? ye + 1 : 1);
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
			var result = duration.compareTo(frequentRank.duration);
			return result != 0 ? result : -Integer.compare(rank, frequentRank.rank);
		}
	}

	public static class EloHighlights {
		
		private final int currentRank;
		private final int currentRating;
		private final int bestRank;
		private final LocalDate bestRankDate;
		private final int bestRating;
		private final LocalDate bestRatingDate;

		public EloHighlights(int currentRank, int currentRating, int bestRank, LocalDate bestRankDate, int bestRating, LocalDate bestRatingDate) {
			this.currentRank = currentRank;
			this.currentRating = currentRating;
			this.bestRank = bestRank;
			this.bestRankDate = bestRankDate;
			this.bestRating = bestRating;
			this.bestRatingDate = bestRatingDate;
		}

		public int getCurrentRank() {
			return currentRank;
		}

		public int getCurrentRating() {
			return currentRating;
		}

		public int getBestRank() {
			return bestRank;
		}

		public LocalDate getBestRankDate() {
			return bestRankDate;
		}

		public int getBestRating() {
			return bestRating;
		}

		public LocalDate getBestRatingDate() {
			return bestRatingDate;
		}

		public boolean isEmpty() {
			return currentRank == 0 && bestRank == 0 && bestRating == 0;
		}
	}
}
