package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class TopPerformerRow {

	private final int rank;
	private final int playerId;
	private final String player;
	private final String countryId;
	private final String countryCode;
	private final WonLost wonLost;

	public TopPerformerRow(int rank, int playerId, String player, String countryId, WonLost wonLost) {
		this.rank = rank;
		this.playerId = playerId;
		this.player = player;
		this.countryId = countryId;
		this.countryCode = CountryUtil.getISOAlpha2Code(countryId);
		this.wonLost = wonLost;
	}

	public int getRank() {
		return rank;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getPlayer() {
		return player;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getLost() {
		return wonLost.getLost();
	}
}
