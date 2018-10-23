package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

public class PlayerTournament {

	private final int id;
	private final String name;
	private final List<String> levels;
	private final List<String> surfaces;
	private final Map<String, Integer> speeds;
	private final int eventCount;
	private final String seasons;
	private final String bestResult;
	private final String lastResult;
	private final int lastTournamentEventId;
	private final WonLost wonLost;
	private final int titles;

	public PlayerTournament(int id, String name, List<String> levels, List<String> surfaces, Map<String, Integer> speeds, int eventCount, String seasons, String bestResult, String lastResult, int lastTournamentEventId, WonLost wonLost, int titles) {
		this.id = id;
		this.name = name;
		this.levels = levels;
		this.surfaces = surfaces;
		this.speeds = speeds;
		this.eventCount = eventCount;
		this.seasons = seasons;
		this.bestResult = bestResult;
		this.lastResult = lastResult;
		this.lastTournamentEventId = lastTournamentEventId;
		this.wonLost = wonLost;
		this.titles = titles;
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

	public Map<String, Integer> getSpeeds() {
		return speeds;
	}

	public int getEventCount() {
		return eventCount;
	}

	public String getSeasons() {
		return seasons;
	}

	public String getBestResult() {
		return bestResult;
	}

	public EventResult bestResult() {
		return EventResult.valueOf(bestResult);
	}

	public String getLastResult() {
		return lastResult;
	}

	public EventResult lastResult() {
		return EventResult.valueOf(lastResult);
	}

	public int getLastTournamentEventId() {
		return lastTournamentEventId;
	}

	public String getWonLost() {
		return wonLost.getWL();
	}

	public String getWonPct() {
		return wonLost.getWonPctStr();
	}

	public double wonLost() {
		return wonLost.getWonPct();
	}

	public int getTitles() {
		return titles;
	}
}
