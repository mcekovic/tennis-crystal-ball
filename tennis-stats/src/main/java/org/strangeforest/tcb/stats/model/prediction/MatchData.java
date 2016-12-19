package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;

public class MatchData {

	private final Date date;
	private final String level;
	private final String surface;
	private final String round;
	private final int opponentId;
	private final int pMatches;
	private final int oMatches;
	private final int pSets;
	private final int oSets;

	public MatchData(Date date, String level, String surface, String round, int opponentId, int pMatches, int oMatches, int pSets, int oSets) {
		this.date = date;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.opponentId = opponentId;
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

	public int getPMatches() {
		return pMatches;
	}

	public int getOMatches() {
		return oMatches;
	}

	public int getPSets() {
		return pSets;
	}

	public int getOSets() {
		return oSets;
	}
}
