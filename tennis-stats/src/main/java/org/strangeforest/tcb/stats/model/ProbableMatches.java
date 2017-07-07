package org.strangeforest.tcb.stats.model;

import java.util.*;

public class ProbableMatches {

	private final TournamentEventResults results;
	private final List<MatchPlayer> players;

	public ProbableMatches(TournamentEventResults results, List<MatchPlayer> players) {
		this.results = results;
		this.players = players;
	}

	public TournamentEventResults getResults() {
		return results;
	}

	public List<MatchPlayer> getPlayers() {
		return players;
	}
}
