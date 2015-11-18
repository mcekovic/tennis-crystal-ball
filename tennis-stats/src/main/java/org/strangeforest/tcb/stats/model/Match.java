package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final long id;
	private final Date date;
	private final String tournament;
	private final String level;
	private final String surface;
	private final String round;
	private final int winnerId;
	private final String winner;
	private final int loserId;
	private final String loser;
	private final String score;

	public Match(long id, Date date, String tournament, String level, String surface, String round, int winnerId, String winner, int loserId, String loser, String score) {
		this.id = id;
		this.date = date;
		this.tournament = tournament;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.winnerId = winnerId;
		this.winner = winner;
		this.loserId = loserId;
		this.loser = loser;
		this.score = score;
	}

	public long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getRound() {
		return round;
	}

	public int getWinnerId() {
		return winnerId;
	}

	public String getWinner() {
		return winner;
	}

	public int getLoserId() {
		return loserId;
	}

	public String getLoser() {
		return loser;
	}

	public String getScore() {
		return score;
	}
}
