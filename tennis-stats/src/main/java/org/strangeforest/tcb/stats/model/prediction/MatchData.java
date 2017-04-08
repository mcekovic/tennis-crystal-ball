package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;

public final class MatchData {

	private final Date date;
	private final String level;
	private final String surface;
	private final String round;
	private final int opponentId;
	private final Integer opponentRank;
	private final String opponentHand;
	private final String opponentBackhand;
	private final String opponentEntry;
	private final int pMatches;
	private final int oMatches;
	private final int pSets;
	private final int oSets;

	public MatchData(Date date, String level, String surface, String round, int opponentId, Integer opponentRank, String opponentHand, String opponentBackhand, String opponentEntry, int pMatches, int oMatches, int pSets, int oSets) {
		this.date = date;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.opponentId = opponentId;
		this.opponentRank = opponentRank;
		this.opponentHand = opponentHand;
		this.opponentBackhand = opponentBackhand;
		this.opponentEntry = opponentEntry;
		this.pMatches = pMatches;
		this.oMatches = oMatches;
		this.pSets = pSets;
		this.oSets = oSets;
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

	public String getRound() {
		return round;
	}

	public int getOpponentId() {
		return opponentId;
	}

	public Integer getOpponentRank() {
		return opponentRank;
	}

	public String getOpponentHand() {
		return opponentHand;
	}

	public String getOpponentBackhand() {
		return opponentBackhand;
	}

	public String getOpponentEntry() {
		return opponentEntry;
	}

	public int getPMatches() {
		return pMatches;
	}

	public int getOMatches() {
		return oMatches;
	}

	public int getMatches() {
		return pMatches + oMatches;
	}

	public int getPSets() {
		return pSets;
	}

	public int getOSets() {
		return oSets;
	}

	public int getSets() {
		return pSets + oSets;
	}
}
