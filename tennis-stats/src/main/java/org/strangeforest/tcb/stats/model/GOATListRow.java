package org.strangeforest.tcb.stats.model;

import java.util.*;

public class GOATListRow extends PlayerRow {

	private final int goatPoints;
	private final int tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints;
	private int yearEndRankGoatPoints, bestRankGoatPoints, bestEloRatingGoatPoints, weeksAtNo1GoatPoints;
	private int bigWinsGoatPoints, grandSlamGoatPoints, bestSeasonGoatPoints, greatestRivalriesGoatPoints, performanceGoatPoints, statisticsGoatPoints;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;
	private int bigTitles;
	private int titles;
	private int bestEloRating;
	private Date bestEloRatingDate;

	public GOATListRow(int goatRank, int playerId, String name, String countryId, int goatPoints, int tournamentGoatPoints, int rankingGoatPoints, int achievementsGoatPoints) {
		super(goatRank, playerId, name, countryId);
		this.goatPoints = goatPoints;
		this.tournamentGoatPoints = tournamentGoatPoints;
		this.rankingGoatPoints = rankingGoatPoints;
		this.achievementsGoatPoints = achievementsGoatPoints;
	}

	public int getGoatRank() {
		return getRank();
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public int getTournamentGoatPoints() {
		return tournamentGoatPoints;
	}

	public int getRankingGoatPoints() {
		return rankingGoatPoints;
	}

	public int getAchievementsGoatPoints() {
		return achievementsGoatPoints;
	}


	// Ranking GOAT points

	public int getYearEndRankGoatPoints() {
		return yearEndRankGoatPoints;
	}

	public void setYearEndRankGoatPoints(int yearEndRankGoatPoints) {
		this.yearEndRankGoatPoints = yearEndRankGoatPoints;
	}

	public int getBestRankGoatPoints() {
		return bestRankGoatPoints;
	}

	public void setBestRankGoatPoints(int bestRankGoatPoints) {
		this.bestRankGoatPoints = bestRankGoatPoints;
	}

	public int getBestEloRatingGoatPoints() {
		return bestEloRatingGoatPoints;
	}

	public void setBestEloRatingGoatPoints(int bestEloRatingGoatPoints) {
		this.bestEloRatingGoatPoints = bestEloRatingGoatPoints;
	}

	public int getWeeksAtNo1GoatPoints() {
		return weeksAtNo1GoatPoints;
	}

	public void setWeeksAtNo1GoatPoints(int weeksAtNo1GoatPoints) {
		this.weeksAtNo1GoatPoints = weeksAtNo1GoatPoints;
	}


	// Achievements GOAT points

	public int getBigWinsGoatPoints() {
		return bigWinsGoatPoints;
	}

	public void setBigWinsGoatPoints(int bigWinsGoatPoints) {
		this.bigWinsGoatPoints = bigWinsGoatPoints;
	}

	public int getGrandSlamGoatPoints() {
		return grandSlamGoatPoints;
	}

	public void setGrandSlamGoatPoints(int grandSlamGoatPoints) {
		this.grandSlamGoatPoints = grandSlamGoatPoints;
	}

	public int getBestSeasonGoatPoints() {
		return bestSeasonGoatPoints;
	}

	public void setBestSeasonGoatPoints(int bestSeasonGoatPoints) {
		this.bestSeasonGoatPoints = bestSeasonGoatPoints;
	}

	public int getGreatestRivalriesGoatPoints() {
		return greatestRivalriesGoatPoints;
	}

	public void setGreatestRivalriesGoatPoints(int greatestRivalriesGoatPoints) {
		this.greatestRivalriesGoatPoints = greatestRivalriesGoatPoints;
	}

	public int getPerformanceGoatPoints() {
		return performanceGoatPoints;
	}

	public void setPerformanceGoatPoints(int performanceGoatPoints) {
		this.performanceGoatPoints = performanceGoatPoints;
	}

	public int getStatisticsGoatPoints() {
		return statisticsGoatPoints;
	}

	public void setStatisticsGoatPoints(int statisticsGoatPoints) {
		this.statisticsGoatPoints = statisticsGoatPoints;
	}


	// Titles

	public int getGrandSlams() {
		return grandSlams;
	}

	public void setGrandSlams(int grandSlams) {
		this.grandSlams = grandSlams;
	}

	public int getTourFinals() {
		return tourFinals;
	}

	public void setTourFinals(int tourFinals) {
		this.tourFinals = tourFinals;
	}

	public int getMasters() {
		return masters;
	}

	public void setMasters(int masters) {
		this.masters = masters;
	}

	public int getOlympics() {
		return olympics;
	}

	public void setOlympics(int olympics) {
		this.olympics = olympics;
	}

	public int getBigTitles() {
		return bigTitles;
	}

	public void setBigTitles(int bigTitles) {
		this.bigTitles = bigTitles;
	}

	public int getTitles() {
		return titles;
	}

	public void setTitles(int titles) {
		this.titles = titles;
	}


	// Elo rating

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
}
