package org.strangeforest.tcb.stats.model;

import java.util.*;

public class CurrentMatch {

	private final long id;
	private final int currentEventId;
	private final short matchNum;
	private final long prevId;
	private final long nextId;
	private final Date date;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final String round;
	private final short bestOf;
	private final MatchPlayer player1;
	private final MatchPlayer player2;
	private final short winner;
	private final String score;
	private final String outcome;

	public CurrentMatch(long id, int currentEventId, short matchNum, long prevId, long nextId,
	                    Date date, String level, String surface, boolean indoor, String round, short bestOf,
	                    MatchPlayer player1, MatchPlayer player2, short winner, String score, String outcome) {
		this.id = id;
		this.currentEventId = currentEventId;
		this.matchNum = matchNum;
		this.prevId = prevId;
		this.nextId = nextId;
		this.date = date;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.round = round;
		this.bestOf = bestOf;
		this.player1 = player1;
		this.player2 = player2;
		this.winner = winner;
		this.score = score;
		this.outcome = outcome;
	}

	public long getId() {
		return id;
	}

	public int getCurrentEventId() {
		return currentEventId;
	}

	public short getMatchNum() {
		return matchNum;
	}

	public long getPrevId() {
		return prevId;
	}

	public long getNextId() {
		return nextId;
	}

	public Date getDate() {
		return date;
	}

	public String getLevel() {
		return level;
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

	public short getBestOf() {
		return bestOf;
	}

	public MatchPlayer getPlayer1() {
		return player1;
	}

	public MatchPlayer getPlayer2() {
		return player2;
	}

	public short getWinner() {
		return winner;
	}

	public String getScore() {
		return score;
	}

	public String getOutcome() {
		return outcome;
	}
}
