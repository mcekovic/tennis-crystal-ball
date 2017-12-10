package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.forecast.*;

public class ProbableMatches {

	private final InProgressEvent event;
	private final TournamentEventResults results;
	private final List<MatchPlayer> players;

	public ProbableMatches(InProgressEvent event, TournamentEventResults results, List<MatchPlayer> players) {
		this.event = event;
		this.results = results;
		this.players = players;
	}

	public InProgressEvent getEvent() {
		return event;
	}

	public TournamentEventResults getResults() {
		return results;
	}

	public List<MatchPlayer> getPlayers() {
		return players;
	}
}
