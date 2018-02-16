package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

public class PlayerPath {

	private final TournamentEventResults completedMatches;
	private final PlayerPathMatches probableMatches;
	private final List<MatchPlayer> players;

	public PlayerPath(TournamentEventResults completedMatches, PlayerPathMatches probableMatches, List<MatchPlayer> players) {
		this.completedMatches = completedMatches;
		this.probableMatches = probableMatches;
		this.players = players;
	}

	public TournamentEventResults getCompletedMatches() {
		return completedMatches;
	}

	public PlayerPathMatches getProbableMatches() {
		return probableMatches;
	}

	public List<MatchPlayer> getPlayers() {
		return players;
	}

	public boolean isEmpty() {
		return completedMatches.isEmpty() && probableMatches.isEmpty();
	}
}
