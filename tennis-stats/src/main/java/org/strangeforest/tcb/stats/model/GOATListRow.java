package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class GOATListRow {

	private final int playerId;
	private final int goatRank;
	private final String name;
	private final String countryId;
	private final String countryCode;
	private int goatPoints;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;
	private int bigTitles;
	private int titles;

	public GOATListRow(int playerId, int goatRank, String countryId, String name, int goatPoints) {
		this.goatRank = goatRank;
		this.countryId = countryId;
		this.playerId = playerId;
		this.countryCode = CountryUtil.getISOAlpha2Code(countryId);
		this.name = name;
		this.goatPoints = goatPoints;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getGoatRank() {
		return goatRank;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getName() {
		return name;
	}

	public int getGoatPoints() {
		return goatPoints;
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
