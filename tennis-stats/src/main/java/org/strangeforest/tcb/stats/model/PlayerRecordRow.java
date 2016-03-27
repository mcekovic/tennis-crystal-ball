package org.strangeforest.tcb.stats.model;

public class PlayerRecordRow extends PlayerRow {

	private final int value;

	public PlayerRecordRow(int rank, int playerId, String name, String countryId, boolean active, int value) {
		super(rank, playerId, name, countryId, active);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
