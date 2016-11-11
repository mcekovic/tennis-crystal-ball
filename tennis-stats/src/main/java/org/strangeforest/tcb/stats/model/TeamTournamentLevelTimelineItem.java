package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class TeamTournamentLevelTimelineItem {

	private final int season;
	private final int tournamentEventId;
	private final String surface;
	private Country winner;
	private Country runnerUp;
	private String score;

	public TeamTournamentLevelTimelineItem(int season, int tournamentEventId, String surface) {
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.surface = surface;
	}

	public int getSeason() {
		return season;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getSurface() {
		return surface;
	}

	public Country getWinner() {
		return winner;
	}

	public void setWinnerId(String winnerId) {
		winner = new Country(winnerId);
	}

	public Country getRunnerUp() {
		return runnerUp;
	}

	public void setRunnerUpId(String runnerUpId) {
		runnerUp = new Country(runnerUpId);
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}
}
