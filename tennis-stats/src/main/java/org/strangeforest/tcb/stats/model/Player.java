package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.util.*;

public class Player {

	// General
	private final int id;
	private String name;
	private Date dob;
	private int age;
	private Country country;
	private String birthplace;
	private String residence;
	private int height;
	private int weight;
	private String hand;
	private String backhand;

	// Titles
	private int titles;
	private int grandSlams;
	private int tourFinals;
	private int masters;
	private int olympics;

	// Ranking
	private int currentRank;
	private int currentRankPoints;
	private int bestRank;
	private Date bestRankDate;
	private int bestRankPoints;
	private Date bestRankPointsDate;
	private int goatRank;
	private int goatPoints;
	private int weeksAtNo1;

	// Tennis
	private boolean active;
	private int turnedPro;
	private String coach;

	// Social
	private String webSite;
	private String twitter;
	private String facebook;

	public Player(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}


	// General

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountryId(String countryId) {
		country = new Country(countryId);
	}

	public String getBirthplace() {
		return birthplace;
	}

	public void setBirthplace(String birthplace) {
		this.birthplace = birthplace;
	}

	public String getResidence() {
		return residence;
	}

	public void setResidence(String residence) {
		this.residence = residence;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getHand() {
		return hand;
	}

	public String getHandName() {
		switch (hand) {
			case "L": return "Left-handed";
			case "R": return "Right-handed";
			default: return null;
		}
	}

	public void setHand(String hand) {
		this.hand = hand;
	}

	public String getBackhand() {
		return backhand;
	}

	public String getBackhandName() {
		switch (backhand) {
			case "1": return "One-handed";
			case "2": return "Two-handed";
			default: return null;
		}
	}

	public void setBackhand(String backhand) {
		this.backhand = backhand;
	}


	// Titles

	public int getTitles() {
		return titles;
	}

	public void setTitles(int titles) {
		this.titles = titles;
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

	public Date getBestRankDate() {
		return bestRankDate;
	}

	public void setBestRankDate(Date bestRankDate) {
		this.bestRankDate = bestRankDate;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public Date getBestRankPointsDate() {
		return bestRankPointsDate;
	}

	public void setBestRankPointsDate(Date bestRankPointsDate) {
		this.bestRankPointsDate = bestRankPointsDate;
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

	public int getWeeksAtNo1() {
		return weeksAtNo1;
	}

	public void setWeeksAtNo1(int weeksAtNo1) {
		this.weeksAtNo1 = weeksAtNo1;
	}


	// Tennis

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getTurnedPro() {
		return turnedPro;
	}

	public void setTurnedPro(int turnedPro) {
		this.turnedPro = turnedPro;
	}

	public String getCoach() {
		return coach;
	}

	public void setCoach(String coach) {
		this.coach = coach;
	}


	// Social

	public String getWebSite() {
		return webSite;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}
}
