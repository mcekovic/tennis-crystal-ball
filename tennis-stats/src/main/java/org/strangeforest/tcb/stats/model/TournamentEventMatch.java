package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentEventMatch {

	private final long id;
	private final String round;
	private final MatchPlayer winner;
	private final MatchPlayer loser;
	private final List<SetScore> score;
	private final String outcome;
	private final boolean hasStats;

	public TournamentEventMatch(long id, String round, MatchPlayer winner, MatchPlayer loser, List<SetScore> score, String outcome, boolean hasStats) {
		this.id = id;
		this.round = round;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
		this.outcome = outcome;
		this.hasStats = hasStats;
	}

	public long getId() {
		return id;
	}

	public String getRound() {
		return round;
	}

	public MatchPlayer getWinner() {
		return winner;
	}

	public MatchPlayer getLoser() {
		return loser;
	}

	public List<SetScore> getScore() {
		return score;
	}

	public String getOutcome() {
		return outcome;
	}

	public boolean isHasStats() {
		return hasStats;
	}
}
