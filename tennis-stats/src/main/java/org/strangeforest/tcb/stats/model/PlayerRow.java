package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public abstract class PlayerRow {

	private final int rank;
	private final int playerId;
	private final String player;
	private final String countryId;
	private final String countryCode;

	protected PlayerRow(int rank, int playerId, String player, String countryId) {
		this.rank = rank;
		this.playerId = playerId;
		this.player = player;
		this.countryId = countryId;
		this.countryCode = CountryUtil.getISOAlpha2Code(countryId);
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
}
