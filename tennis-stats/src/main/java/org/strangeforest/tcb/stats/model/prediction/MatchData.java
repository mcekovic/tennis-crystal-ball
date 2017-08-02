package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;

public final class MatchData {

	private final LocalDate date;
	private final String level;
	private final String surface;
	private final int tournamentId;
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

	public MatchData(LocalDate date, String level, String surface, int tournamentId, String round, int opponentId, Integer opponentRank, String opponentHand, String opponentBackhand, String opponentEntry, int pMatches, int oMatches, int pSets, int oSets) {
		this.date = date;
		this.level = level;
		this.surface = surface;
		this.tournamentId = tournamentId;
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

	public LocalDate getDate() {
		return date;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public int getTournamentId() {
		return tournamentId;
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
