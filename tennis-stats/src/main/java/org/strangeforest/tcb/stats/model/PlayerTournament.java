package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerTournament {

	private final int id;
	private final String name;
	private final List<String> levels;
	private final List<String> surfaces;
	private final int eventCount;
	private final String seasons;

	public PlayerTournament(int id, String name, List<String> levels, List<String> surfaces, int eventCount, String seasons) {
		this.id = id;
		this.name = name;
		this.levels = levels;
		this.surfaces = surfaces;
		this.eventCount = eventCount;
		this.seasons = seasons;
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
}
