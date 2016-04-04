package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerGOATPoints {

	// Totals
	private final int totalPoints;
	private int tournamentPoints;
	private int rankingPoints;
	private int achievementsPoints;
	// Ranking
	private int yearEndRankPoints;
	private int bestRankPoints;
	private int bestEloRatingPoints;
	private int weeksAtNo1Points;
	// Achievements
	private int bigWinsPoints;
	private int grandSlamPoints;
	private int bestSeasonPoints;
	private int greatestRivalriesPoints;
	private int performancePoints;
	private int statisticsPoints;
	// Seasons
	private List<PlayerSeasonGOATPoints> seasonsPoints = new ArrayList<>();

	public PlayerGOATPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}


	// Totals

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getTournamentPoints() {
		return tournamentPoints;
	}

	public void setTournamentPoints(int tournamentPoints) {
		this.tournamentPoints = tournamentPoints;
	}

	public int getRankingPoints() {
		return rankingPoints;
	}

	public void setRankingPoints(int rankingPoints) {
		this.rankingPoints = rankingPoints;
	}

	public int getAchievementsPoints() {
		return achievementsPoints;
	}

	public void setAchievementsPoints(int achievementsPoints) {
		this.achievementsPoints = achievementsPoints;
	}


	// Ranking

	public int getYearEndRankPoints() {
		return yearEndRankPoints;
	}

	public void setYearEndRankPoints(int yearEndRankPoints) {
		this.yearEndRankPoints = yearEndRankPoints;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public int getBestEloRatingPoints() {
		return bestEloRatingPoints;
	}

	public void setBestEloRatingPoints(int bestEloRatingPoints) {
		this.bestEloRatingPoints = bestEloRatingPoints;
	}

	public int getWeeksAtNo1Points() {
		return weeksAtNo1Points;
	}

	public void setWeeksAtNo1Points(int weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}


	// Achievements

	public int getBigWinsPoints() {
		return bigWinsPoints;
	}

	public void setBigWinsPoints(int bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}

	public int getGrandSlamPoints() {
		return grandSlamPoints;
	}

	public void setGrandSlamPoints(int grandSlamPoints) {
		this.grandSlamPoints = grandSlamPoints;
	}

	public int getBestSeasonPoints() {
		return bestSeasonPoints;
	}

	public void setBestSeasonPoints(int bestSeasonPoints) {
		this.bestSeasonPoints = bestSeasonPoints;
	}

	public int getGreatestRivalriesPoints() {
		return greatestRivalriesPoints;
	}

	public void setGreatestRivalriesPoints(int greatestRivalriesPoints) {
		this.greatestRivalriesPoints = greatestRivalriesPoints;
	}

	public int getPerformancePoints() {
		return performancePoints;
	}

	public void setPerformancePoints(int performancePoints) {
		this.performancePoints = performancePoints;
	}

	public int getStatisticsPoints() {
		return statisticsPoints;
	}

	public void setStatisticsPoints(int statisticsPoints) {
		this.statisticsPoints = statisticsPoints;
	}


	// Seasons

	public List<PlayerSeasonGOATPoints> getSeasonsPoints() {
		return seasonsPoints;
	}

	public void setSeasonsPoints(List<PlayerSeasonGOATPoints> seasonsPoints) {
		this.seasonsPoints = seasonsPoints;
	}

	public PlayerSeasonGOATPoints getSeasonPoints(int season) {
		return seasonsPoints.stream().filter( points -> points.getSeason() == season).findFirst().orElse(new PlayerSeasonGOATPoints(season, 0));
	}
}
