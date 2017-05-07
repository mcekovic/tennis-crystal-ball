package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.service.*;

public class PlayerSeason {

	private final WonLost overall;
	private final Map<Surface, WonLost> surfaceMatches;
	private final Map<TournamentLevel, WonLost> levelMatches;
	private final Map<Opponent, WonLost> oppositionMatches;
	private final Map<Round, WonLost> roundMatches;

	public PlayerSeason(WonLost overall) {
		this.overall = overall;
		surfaceMatches = new LinkedHashMap<>();
		levelMatches = new LinkedHashMap<>();
		oppositionMatches = new TreeMap<>();
		roundMatches = new LinkedHashMap<>();
	}

	public WonLost getOverall() {
		return overall;
	}

	public Map<Surface, WonLost> getSurfaceMatches() {
		return surfaceMatches;
	}

	public void addSurfaceMatches(Surface surface, WonLost wonLost) {
		if (!wonLost.isEmpty())
			surfaceMatches.put(surface, wonLost);
	}

	public Map<TournamentLevel, WonLost> getLevelMatches() {
		return levelMatches;
	}

	public void addLevelMatches(TournamentLevel level, WonLost wonLost) {
		if (!wonLost.isEmpty())
			levelMatches.put(level, wonLost);
	}

	public Map<Opponent, WonLost> getOppositionMatches() {
		return oppositionMatches;
	}

	public void addOppositionMatches(Opponent opposition, WonLost wonLost) {
		if (!wonLost.isEmpty())
			oppositionMatches.put(opposition, wonLost);
	}

	public void processOpposition() {
		WonLost wonLost = WonLost.EMPTY;
		for (Map.Entry<Opponent, WonLost> entry : oppositionMatches.entrySet()) {
			wonLost = wonLost.add(entry.getValue());
			entry.setValue(wonLost);
		}
	}

	public Map<Round, WonLost> getRoundMatches() {
		return roundMatches;
	}

	public void addRoundMatches(Round round, WonLost wonLost) {
		if (!wonLost.isEmpty())
			roundMatches.put(round, wonLost);
	}
}
