package org.strangeforest.tcb.stats.model;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

public class GOATListRow extends PlayerRow {

	private final int goatPoints;
	private final int tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints;
	private int yearEndRankGoatPoints, bestRankGoatPoints, weeksAtNo1GoatPoints, weeksAtEloTopNGoatPoints, bestEloRatingGoatPoints;
	private int grandSlamGoatPoints, bigWinsGoatPoints, h2hGoatPoints, recordsGoatPoints, bestSeasonGoatPoints, greatestRivalriesGoatPoints, performanceGoatPoints, statisticsGoatPoints;
	private int grandSlams;
	private int tourFinals;
	private int altFinals;
	private int masters;
	private int olympics;
	private int bigTitles;
	private int titles;
	private int weeksAtNo1;
	private WonLost wonLost;
	private int bestEloRating;
	private LocalDate bestEloRatingDate;

	public GOATListRow(int goatRank, int playerId, String name, String countryId, Boolean active, int goatPoints, int tournamentGoatPoints, int rankingGoatPoints, int achievementsGoatPoints) {
		super(goatRank, playerId, name, countryId, active);
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

	public int getWeeksAtNo1GoatPoints() {
		return weeksAtNo1GoatPoints;
	}

	public void setWeeksAtNo1GoatPoints(int weeksAtNo1GoatPoints) {
		this.weeksAtNo1GoatPoints = weeksAtNo1GoatPoints;
	}

	public int getWeeksAtEloTopNGoatPoints() {
		return weeksAtEloTopNGoatPoints;
	}

	public void setWeeksAtEloTopNGoatPoints(int weeksAtEloTopNGoatPoints) {
		this.weeksAtEloTopNGoatPoints = weeksAtEloTopNGoatPoints;
	}

	public int getBestEloRatingGoatPoints() {
		return bestEloRatingGoatPoints;
	}

	public void setBestEloRatingGoatPoints(int bestEloRatingGoatPoints) {
		this.bestEloRatingGoatPoints = bestEloRatingGoatPoints;
	}

	
	// Achievements GOAT points

	public int getGrandSlamGoatPoints() {
		return grandSlamGoatPoints;
	}

	public void setGrandSlamGoatPoints(int grandSlamGoatPoints) {
		this.grandSlamGoatPoints = grandSlamGoatPoints;
	}

	public int getBigWinsGoatPoints() {
		return bigWinsGoatPoints;
	}

	public void setBigWinsGoatPoints(int bigWinsGoatPoints) {
		this.bigWinsGoatPoints = bigWinsGoatPoints;
	}

	public int getH2hGoatPoints() {
		return h2hGoatPoints;
	}

	public void setH2hGoatPoints(int h2hGoatPoints) {
		this.h2hGoatPoints = h2hGoatPoints;
	}

	public int getRecordsGoatPoints() {
		return recordsGoatPoints;
	}

	public void setRecordsGoatPoints(int recordsGoatPoints) {
		this.recordsGoatPoints = recordsGoatPoints;
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

	public int getAltFinals() {
		return altFinals;
	}

	public void setAltFinals(int altFinals) {
		this.altFinals = altFinals;
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


	// Weeks at No. 1

	public int getWeeksAtNo1() {
		return weeksAtNo1;
	}

	public void setWeeksAtNo1(int weeksAtNo1) {
		this.weeksAtNo1 = weeksAtNo1;
	}


	// Won/Lost

	public String getWonPct() {
		return wonLost.getWonPctStr();
	}

	public String getWonLost() {
		return wonLost.getWL();
	}

	public void setWonLost(WonLost wonLost) {
		this.wonLost = wonLost;
	}


	// Elo rating

	public int getBestEloRating() {
		return bestEloRating;
	}

	public void setBestEloRating(int bestEloRating) {
		this.bestEloRating = bestEloRating;
	}

	public LocalDate getBestEloRatingDate() {
		return bestEloRatingDate;
	}

	public void setBestEloRatingDate(LocalDate bestEloRatingDate) {
		this.bestEloRatingDate = bestEloRatingDate;
	}
}
