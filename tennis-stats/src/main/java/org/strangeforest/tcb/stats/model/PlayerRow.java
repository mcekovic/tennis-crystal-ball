package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public abstract class PlayerRow {

	private final int rank;
	private final int playerId;
	private final String name;
	private final String countryId;
	private final String countryCode;

	protected PlayerRow(int rank, int playerId, String name, String countryId) {
		this.rank = rank;
		this.playerId = playerId;
		this.name = name;
		this.countryId = countryId;
		this.countryCode = CountryUtil.getISOAlpha2Code(countryId);
	}

	public int getRank() {
		return rank;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getName() {
		return name;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountryCode() {
		return countryCode;
	}
}
