package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final Date date;
	private final String tournament;
	private final String winner;
	private final String loser;
	private final String score;

	public Match(Date date, String tournament, String winner, String loser, String score) {
		this.date = date;
		this.tournament = tournament;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
	}

	public Date getDate() {
		return date;
	}

	public String getTournament() {
		return tournament;
	}

	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}

	public String getScore() {
		return score;
	}
}
