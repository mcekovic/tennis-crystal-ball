package org.strangeforest.tcb.stats.model;

import java.util.*;

public class ProbableMatches {

	private final TournamentEventResults results;
	private final List<MatchPlayerEx> players;

	public ProbableMatches(TournamentEventResults results, List<MatchPlayerEx> players) {
		this.results = results;
		this.players = players;
	}

	public TournamentEventResults getResults() {
		return results;
	}

	public List<MatchPlayerEx> getPlayers() {
		return players;
	}
}
