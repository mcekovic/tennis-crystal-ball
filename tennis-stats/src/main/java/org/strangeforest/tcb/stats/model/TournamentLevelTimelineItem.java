package org.strangeforest.tcb.stats.model;

import java.time.*;

public class TournamentLevelTimelineItem {

	private final int tournamentId;
	private final String name;
	private final int season;
	private final int tournamentEventId;
	private final LocalDate date;
	private final String level;
	private final String surface;
	private TournamentLevelTimelinePlayer winner;
	private int playerWins;
	private TournamentLevelTimelinePlayer runnerUp;
	private String score;
	private String outcome;

	public TournamentLevelTimelineItem(int tournamentId, String name, int season, int tournamentEventId, LocalDate date, String level, String surface) {
		this.tournamentId = tournamentId;
		this.name = name;
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.date = date;
		this.level = level;
		this.surface = surface;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getName() {
		return name;
	}

	public int getSeason() {
		return season;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public TournamentLevelTimelinePlayer getWinner() {
		return winner;
	}

	public void setWinner(TournamentLevelTimelinePlayer winner) {
		this.winner = winner;
	}

	public int getPlayerWins() {
		return playerWins;
	}

	public void setPlayerWins(int playerWins) {
		this.playerWins = playerWins;
	}

	public TournamentLevelTimelinePlayer getRunnerUp() {
		return runnerUp;
	}

	public void setRunnerUp(TournamentLevelTimelinePlayer runnerUp) {
		this.runnerUp = runnerUp;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
}
