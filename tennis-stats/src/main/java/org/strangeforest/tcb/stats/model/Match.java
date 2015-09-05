package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final Date date;
	private final String tournament;
	private final int winnerId;
	private final String winner;
	private final int loserId;
	private final String loser;
	private final String score;

	public Match(Date date, String tournament, int winnerId, String winner, int loserId, String loser, String score) {
		this.date = date;
		this.tournament = tournament;
		this.winnerId = winnerId;
		this.winner = winner;
		this.loserId = loserId;
		this.loser = loser;
		this.score = score;
	}

	public Date getDate() {
		return date;
	}

	public String getTournament() {
		return tournament;
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
