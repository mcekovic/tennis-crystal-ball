package org.strangeforest.tcb.stats.model;

public class GOATListRow {

	private int goatRanking;
	private String name;
	private int goatPoints;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;
	private int titles;

	public GOATListRow(int goatRanking, String name, int goatPoints) {
		this.goatRanking = goatRanking;
		this.name = name;
		this.goatPoints = goatPoints;
	}

	public int getGoatRanking() {
		return goatRanking;
	}

	public String getName() {
		return name;
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public void setGoatRanking(int goatRanking) {
		this.goatRanking = goatRanking;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGoatPoints(int goatPoints) {
		this.goatPoints = goatPoints;
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

	public int getTitles() {
		return titles;
	}

	public void setTitles(int titles) {
		this.titles = titles;
	}
}
