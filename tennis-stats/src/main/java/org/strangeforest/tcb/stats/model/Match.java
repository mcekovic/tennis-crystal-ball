package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final Date date;
	private final String name;
	private final String winner;
	private final String loser;
	private final String result;

	public Match(Date date, String name, String winner, String loser, String result) {
		this.date = date;
		this.name = name;
		this.winner = winner;
		this.loser = loser;
		this.result = result;
	}

	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}

	public String getResult() {
		return result;
	}
}
