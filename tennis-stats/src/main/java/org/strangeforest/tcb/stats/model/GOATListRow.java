package org.strangeforest.tcb.stats.model;

public class GOATListRow extends PlayerRow {

	private final int goatPoints;
	private final int tournamentGoatPoints;
	private final int rankingGoatPoints;
	private final int achievementsGoatPoints;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;
	private int bigTitles;
	private int titles;

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
}
