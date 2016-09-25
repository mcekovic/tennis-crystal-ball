package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentLevelTimelineItem {

	private final int tournamentId;
	private final String name;
	private final int season;
	private final int tournamentEventId;
	private final Date date;
	private final String surface;
	private PlayerRow player;
	private int playerWins;
	private MatchPlayer winner;
	private MatchPlayer runnerUp;
	private String score;
	private String outcome;

	public TournamentLevelTimelineItem(int tournamentId, String name, int season, int tournamentEventId, Date date, String surface) {
		this.tournamentId = tournamentId;
		this.name = name;
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.date = date;
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

	public Date getDate() {
		return date;
	}

	public String getSurface() {
		return surface;
	}

	public PlayerRow getPlayer() {
		return player;
	}

	public void setPlayer(PlayerRow player) {
		this.player = player;
	}

	public int getPlayerWins() {
		return playerWins;
	}

	public void setPlayerWins(int playerWins) {
		this.playerWins = playerWins;
	}

	public MatchPlayer getWinner() {
		return winner;
	}

	public void setWinner(MatchPlayer winner) {
		this.winner = winner;
	}

	public MatchPlayer getRunnerUp() {
		return runnerUp;
	}

	public void setRunnerUp(MatchPlayer runnerUp) {
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
