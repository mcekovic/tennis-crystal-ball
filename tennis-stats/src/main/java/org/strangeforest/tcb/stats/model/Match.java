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
	private final int winnerSeed;
	private final String winnerEntry;
	private final int loserId;
	private final String loser;
	private final int loserSeed;
	private final String loserEntry;
	private final String score;

	public Match(long id, Date date, String tournament, String level, String surface, String round,
	             int winnerId, String winner, int winnerSeed, String winnerEntry, int loserId, String loser, int loserSeed, String loserEntry, String score) {
		this.id = id;
		this.date = date;
		this.tournament = tournament;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.winnerId = winnerId;
		this.winner = winner;
		this.winnerSeed = winnerSeed;
		this.winnerEntry = winnerEntry;
		this.loserId = loserId;
		this.loser = loser;
		this.loserSeed = loserSeed;
		this.loserEntry = loserEntry;
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

	public int getWinnerSeed() {
		return winnerSeed;
	}

	public String getWinnerEntry() {
		return winnerEntry;
	}

	public int getLoserId() {
		return loserId;
	}

	public String getLoser() {
		return loser;
	}

	public int getLoserSeed() {
		return loserSeed;
	}

	public String getLoserEntry() {
		return loserEntry;
	}

	public String getScore() {
		return score;
	}
}
