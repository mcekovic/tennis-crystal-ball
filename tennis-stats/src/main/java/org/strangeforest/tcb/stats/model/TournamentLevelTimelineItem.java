package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentLevelTimelineItem {

	private final int tournamentId;
	private final String name;
	private final int season;
	private final int tournamentEventId;
	private final Date date;
	private final String surface;
	private PlayerRow winner;
	private int winnerWins;

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

	public PlayerRow getWinner() {
		return winner;
	}

	public void setWinner(PlayerRow winner) {
		this.winner = winner;
	}

	public int getWinnerWins() {
		return winnerWins;
	}

	public void setWinnerWins(int winnerWins) {
		this.winnerWins = winnerWins;
	}
}
