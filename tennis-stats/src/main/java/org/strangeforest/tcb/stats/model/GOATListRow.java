package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class GOATListRow {

	private final int goatRank;
	private final int playerId;
	private final String player;
	private final String countryId;
	private final String countryCode;
	private int goatPoints;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;
	private int bigTitles;
	private int titles;

	public GOATListRow(int goatRank, int playerId, String player, String countryId, int goatPoints) {
		this.goatRank = goatRank;
		this.playerId = playerId;
		this.player = player;
		this.countryId = countryId;
		this.countryCode = CountryUtil.getISOAlpha2Code(countryId);
		this.goatPoints = goatPoints;
	}

	public int getGoatRank() {
		return goatRank;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getPlayer() {
		return player;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountryCode() {
		return countryCode;
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
