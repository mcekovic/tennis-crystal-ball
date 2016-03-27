package org.strangeforest.tcb.stats.model;

public class BestSeasonRow extends PlayerRow {

	private final int season;
	private final int goatPoints;
	private int tournamentGoatPoints, yearEndRankGoatPoints, weeksAtNo1GoatPoints, bigWinsGoatPoints, grandSlamGoatPoints;
	private int grandSlamTitles, grandSlamFinals, grandSlamSemiFinals;
	private int tourFinalsTitles, tourFinalsFinals;
	private int mastersTitles, mastersFinals;
	private int olympicsTitles;
	private int titles;
	private Integer yearEndRank;

	public BestSeasonRow(int seasonRank, int playerId, String name, String countryId, int season, int goatPoints) {
		super(seasonRank, playerId, name, countryId, false);
		this.season = season;
		this.goatPoints = goatPoints;
	}

	public int getSeason() {
		return season;
	}

	public int getGoatPoints() {
		return goatPoints;
	}


	// GOAT points items

	public int getTournamentGoatPoints() {
		return tournamentGoatPoints;
	}

	public void setTournamentGoatPoints(int tournamentGoatPoints) {
		this.tournamentGoatPoints = tournamentGoatPoints;
	}

	public int getYearEndRankGoatPoints() {
		return yearEndRankGoatPoints;
	}

	public void setYearEndRankGoatPoints(int yearEndRankGoatPoints) {
		this.yearEndRankGoatPoints = yearEndRankGoatPoints;
	}

	public int getWeeksAtNo1GoatPoints() {
		return weeksAtNo1GoatPoints;
	}

	public void setWeeksAtNo1GoatPoints(int weeksAtNo1GoatPoints) {
		this.weeksAtNo1GoatPoints = weeksAtNo1GoatPoints;
	}

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


	// Titles

	public int getGrandSlamTitles() {
		return grandSlamTitles;
	}

	public void setGrandSlamTitles(int grandSlamTitles) {
		this.grandSlamTitles = grandSlamTitles;
	}

	public int getGrandSlamFinals() {
		return grandSlamFinals;
	}

	public void setGrandSlamFinals(int grandSlamFinals) {
		this.grandSlamFinals = grandSlamFinals;
	}

	public int getGrandSlamSemiFinals() {
		return grandSlamSemiFinals;
	}

	public void setGrandSlamSemiFinals(int grandSlamSemiFinals) {
		this.grandSlamSemiFinals = grandSlamSemiFinals;
	}

	public int getTourFinalsTitles() {
		return tourFinalsTitles;
	}

	public void setTourFinalsTitles(int tourFinalsTitles) {
		this.tourFinalsTitles = tourFinalsTitles;
	}

	public int getTourFinalsFinals() {
		return tourFinalsFinals;
	}

	public void setTourFinalsFinals(int tourFinalsFinals) {
		this.tourFinalsFinals = tourFinalsFinals;
	}

	public int getMastersTitles() {
		return mastersTitles;
	}

	public void setMastersTitles(int mastersTitles) {
		this.mastersTitles = mastersTitles;
	}

	public int getMastersFinals() {
		return mastersFinals;
	}

	public void setMastersFinals(int mastersFinals) {
		this.mastersFinals = mastersFinals;
	}

	public int getOlympicsTitles() {
		return olympicsTitles;
	}

	public void setOlympicsTitles(int olympicsTitles) {
		this.olympicsTitles = olympicsTitles;
	}

	public int getTitles() {
		return titles;
	}

	public void setTitles(int titles) {
		this.titles = titles;
	}

	public Integer getYearEndRank() {
		return yearEndRank;
	}

	public void setYearEndRank(Integer yearEndRank) {
		this.yearEndRank = yearEndRank;
	}
}
