package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final long id;
	private final Date date;
	private final int tournamentEventId;
	private final String tournament;
	private final String level;
	private final int bestOf;
	private final String surface;
	private final boolean indoor;
	private final String round;
	private final MatchPlayer winner;
	private final MatchPlayer loser;
	private final String score;
	private final String outcome;
	private final boolean hasStats;

	public Match(long id, Date date, int tournamentEventId, String tournament, String level, int bestOf, String surface, boolean indoor, String round,
	             MatchPlayer winner, MatchPlayer loser, String score, String outcome, boolean hasStats) {
		this.id = id;
		this.date = date;
		this.tournamentEventId = tournamentEventId;
		this.tournament = tournament;
		this.level = level;
		this.bestOf = bestOf;
		this.surface = surface;
		this.indoor = indoor;
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

	public Date getDate() {
		return date;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public int getBestOf() {
		return bestOf;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
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

	public String getScore() {
		return score;
	}

	public String getOutcome() {
		return outcome;
	}

	public boolean isHasStats() {
		return hasStats;
	}
}
