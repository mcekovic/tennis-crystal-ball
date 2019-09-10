package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public class PlayerTournament {

	private final int id;
	private final String name;
	private final List<String> levels;
	private final List<String> surfaces;
	private final Map<String, Integer> speeds;
	private final int eventCount;
	private final String seasons;
	private final String bestResult, bestLevel;
	private final String lastResult, lastLevel;
	private final int lastTournamentEventId;
	private final WonLost wonLost;
	private final int titles;

	public PlayerTournament(int id, String name, List<String> levels, List<String> surfaces, Map<String, Integer> speeds, int eventCount, String seasons, String bestResult, String bestLevel, String lastResult, String lastLevel, int lastTournamentEventId, WonLost wonLost, int titles) {
		this.id = id;
		this.name = name;
		this.levels = levels;
		this.surfaces = surfaces;
		this.speeds = speeds;
		this.eventCount = eventCount;
		this.seasons = seasons;
		this.bestResult = bestResult;
		this.bestLevel = bestLevel;
		this.lastResult = lastResult;
		this.lastLevel = lastLevel;
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
		return mapResult(bestLevel, bestResult);
	}

	public int bestResultOrder() {
		return EventResult.valueOf(bestResult).getOrder();
	}

	public String getLastResult() {
		return mapResult(lastLevel, lastResult);
	}

	public int lastResultOrder() {
		return EventResult.valueOf(lastResult).getOrder();
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
