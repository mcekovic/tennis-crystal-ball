package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Tournament {

	private final int id;
	private final String extId;
	private final String name;
	private final List<String> levels;
	private final List<String> surfaces;
	private Map<String, Integer> speeds;
	private final int eventCount;
	private final String seasons;
	private double participation;
	private int playerCount;
	private int strength;
	private int averageEloRating;
	private final List<PlayerRow> topPlayers;

	public Tournament(int id, String extId, String name, List<String> levels, List<String> surfaces, Map<String, Integer> speeds, int eventCount, String seasons, int playerCount, double participation, int strength, int averageEloRating, List<PlayerRow> topPlayers) {
		this.id = id;
		this.extId = extId;
		this.name = name;
		this.levels = levels;
		this.surfaces = surfaces;
		this.speeds = speeds;
		this.eventCount = eventCount;
		this.seasons = seasons;
		this.playerCount = playerCount;
		this.participation = participation;
		this.strength = strength;
		this.averageEloRating = averageEloRating;
		this.topPlayers = topPlayers;
	}

	public int getId() {
		return id;
	}

	public String getExtId() {
		return extId;
	}

	public String getName() {
		return name;
	}

	public List<String> getLevels() {
		return levels;
	}

	public List<String> getSurfaces() {
		return surfaces;
	}

	public Map<String, Integer> getSpeeds() {
		return speeds;
	}

	public int getEventCount() {
		return eventCount;
	}

	public String getSeasons() {
		return seasons;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public double getParticipation() {
		return participation;
	}

	public int getStrength() {
		return strength;
	}

	public int getAverageEloRating() {
		return averageEloRating;
	}

	public List<PlayerRow> getTopPlayers() {
		return topPlayers;
	}
}
