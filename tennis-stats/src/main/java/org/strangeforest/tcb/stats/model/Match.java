package org.strangeforest.tcb.stats.model;

import java.time.*;

public class Match {

	private final long id;
	private final LocalDate date;
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
	private Double bigWinPoints;
	private String h2h;

	public Match(long id, LocalDate date, int tournamentEventId, String tournament, String level, int bestOf, String surface, boolean indoor, String round,
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

	public LocalDate getDate() {
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

	public Double getBigWinPoints() {
		return bigWinPoints;
	}

	public void setBigWinPoints(Double bigWinsPoints) {
		this.bigWinPoints = bigWinsPoints;
	}

	public String getH2h() {
		return h2h;
	}

	public void setH2h(String h2h) {
		this.h2h = h2h;
	}
}
