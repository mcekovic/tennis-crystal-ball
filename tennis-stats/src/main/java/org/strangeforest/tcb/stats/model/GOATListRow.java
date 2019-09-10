package org.strangeforest.tcb.stats.model;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

public class GOATListRow extends PlayerRow {

	private final LocalDate dob;
	private final int totalPoints;
	private final int tournamentPoints, rankingPoints, achievementsPoints;
	private int tGPoints, tFLPoints, tMPoints, tOPoints, tABPoints, tDTPoints;
	private int yearEndRankPoints, bestRankPoints, weeksAtNo1Points, weeksAtEloTopNPoints, bestEloRatingPoints;
	private int grandSlamPoints, bigWinsPoints, h2hPoints, recordsPoints, bestSeasonPoints, greatestRivalriesPoints, performancePoints, statisticsPoints;
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

	public GOATListRow(int rank, int playerId, String name, String countryId, Boolean active, LocalDate dob, int totalPoints, int tournamentPoints, int rankingPoints, int achievementsPoints) {
		super(rank, playerId, name, countryId, active);
		this.dob = dob;
		this.totalPoints = totalPoints;
		this.tournamentPoints = tournamentPoints;
		this.rankingPoints = rankingPoints;
		this.achievementsPoints = achievementsPoints;
	}

	public LocalDate getDob() {
		return dob;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getTournamentPoints() {
		return tournamentPoints;
	}

	public int getRankingPoints() {
		return rankingPoints;
	}

	public int getAchievementsPoints() {
		return achievementsPoints;
	}


	// Tournament GOAT points

	public int gettGPoints() {
		return tGPoints;
	}

	public void settGPoints(int tGPoints) {
		this.tGPoints = tGPoints;
	}

	public int gettFLPoints() {
		return tFLPoints;
	}

	public void settFLPoints(int tFLPoints) {
		this.tFLPoints = tFLPoints;
	}

	public int gettMPoints() {
		return tMPoints;
	}

	public void settMPoints(int tMPoints) {
		this.tMPoints = tMPoints;
	}

	public int gettOPoints() {
		return tOPoints;
	}

	public void settOPoints(int tOPoints) {
		this.tOPoints = tOPoints;
	}

	public int gettABPoints() {
		return tABPoints;
	}

	public void settABPoints(int tABPoints) {
		this.tABPoints = tABPoints;
	}

	public int gettDTPoints() {
		return tDTPoints;
	}

	public void settDTPoints(int tDTPoints) {
		this.tDTPoints = tDTPoints;
	}


	// Ranking GOAT points

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

	public int getWeeksAtNo1Points() {
		return weeksAtNo1Points;
	}

	public void setWeeksAtNo1Points(int weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}

	public int getWeeksAtEloTopNPoints() {
		return weeksAtEloTopNPoints;
	}

	public void setWeeksAtEloTopNPoints(int weeksAtEloTopNPoints) {
		this.weeksAtEloTopNPoints = weeksAtEloTopNPoints;
	}

	public int getBestEloRatingPoints() {
		return bestEloRatingPoints;
	}

	public void setBestEloRatingPoints(int bestEloRatingPoints) {
		this.bestEloRatingPoints = bestEloRatingPoints;
	}

	
	// Achievements GOAT points

	public int getGrandSlamPoints() {
		return grandSlamPoints;
	}

	public void setGrandSlamPoints(int grandSlamPoints) {
		this.grandSlamPoints = grandSlamPoints;
	}

	public int getBigWinsPoints() {
		return bigWinsPoints;
	}

	public void setBigWinsPoints(int bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}

	public int getH2hPoints() {
		return h2hPoints;
	}

	public void setH2hPoints(int h2hPoints) {
		this.h2hPoints = h2hPoints;
	}

	public int getRecordsPoints() {
		return recordsPoints;
	}

	public void setRecordsPoints(int recordsPoints) {
		this.recordsPoints = recordsPoints;
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
