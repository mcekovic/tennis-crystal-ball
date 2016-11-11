package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class PlayerRow {

	private final int rank;
	private final int playerId;
	private final String name;
	private final Country country;
	private final Boolean active;

	public PlayerRow(int rank, int playerId, String name, String countryId, Boolean active) {
		this.rank = rank;
		this.playerId = playerId;
		this.name = name;
		country = new Country(countryId);
		this.active = active;
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

	public Country getCountry() {
		return country;
	}

	public Boolean getActive() {
		return active;
	}
}
