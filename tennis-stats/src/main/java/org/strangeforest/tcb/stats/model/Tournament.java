package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Tournament {

	private final int id;
	private final String name;
	private final List<String> levels;
	private final List<String> surfaces;
	private final int eventCount;
	private final String seasons;
	private final int participationPoints;
	private final double participationPct;
	private final List<PlayerRow> topPlayers;

	public Tournament(int id, String name, List<String> levels, List<String> surfaces, int eventCount, String seasons, int participationPoints, double participationPct, List<PlayerRow> topPlayers) {
		this.id = id;
		this.name = name;
		this.levels = levels;
		this.surfaces = surfaces;
		this.eventCount = eventCount;
		this.seasons = seasons;
		this.participationPoints = participationPoints;
		this.participationPct = participationPct;
		this.topPlayers = topPlayers;
	}

	public int getId() {
		return id;
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

	public int getEventCount() {
		return eventCount;
	}

	public String getSeasons() {
		return seasons;
	}

	public int getParticipationPoints() {
		return participationPoints;
	}

	public double getParticipationPct() {
		return participationPct;
	}

	public List<PlayerRow> getTopPlayers() {
		return topPlayers;
	}
}
