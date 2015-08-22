package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import static org.strangeforest.tcb.stats.util.DateUtil.*;

public class Player {

	// General
	private final String name;
	private LocalDate dob;
	private int age;
	private String countryId;
	private String birthplace;
	private String residence;
	private int height;
	private int weight;

	// Tennis
	private String hand;
	private String backhand;
	private int turnedPro;
	private String coach;

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
	private LocalDate bestRankDate;
	private int bestRankPoints;
	private LocalDate bestRankPointsDate;
	private int goatRank;
	private int goatRankPoints;

	// Social
	private String webSite;
	private String twitter;
	private String facebook;

	public Player(String name) {
		this.name = name;
	}


	// General

	public String getName() {
		return name;
	}

	public LocalDate getDob() {
		return dob;
	}

	public Date getDobAsDate() {
		return toDate(dob);
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
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


	// Tennis

	public String getHand() {
		return hand;
	}

	public void setHand(String hand) {
		this.hand = hand;
	}

	public String getBackhand() {
		return backhand;
	}

	public void setBackhand(String backhand) {
		this.backhand = backhand;
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

	public LocalDate getBestRankDate() {
		return bestRankDate;
	}

	public Date getBestRankDateAsDate() {
		return toDate(bestRankDate);
	}

	public void setBestRankDate(LocalDate bestRankDate) {
		this.bestRankDate = bestRankDate;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public LocalDate getBestRankPointsDate() {
		return bestRankPointsDate;
	}

	public Date getBestRankPointsDateAsDate() {
		return toDate(bestRankPointsDate);
	}

	public void setBestRankPointsDate(LocalDate bestRankPointsDate) {
		this.bestRankPointsDate = bestRankPointsDate;
	}

	public int getGoatRank() {
		return goatRank;
	}

	public void setGoatRank(int goatRank) {
		this.goatRank = goatRank;
	}

	public int getGoatRankPoints() {
		return goatRankPoints;
	}

	public void setGoatRankPoints(int goatRankPoints) {
		this.goatRankPoints = goatRankPoints;
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
