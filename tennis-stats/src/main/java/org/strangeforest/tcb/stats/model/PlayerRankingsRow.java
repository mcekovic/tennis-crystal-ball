package org.strangeforest.tcb.stats.model;

public class PlayerRankingsRow extends PlayerRow {

	private final int points;

	public PlayerRankingsRow(int rank, int playerId, String name, String countryId, int points) {
		super(rank, playerId, name, countryId);
		this.points = points;
	}

	public int getPoints() {
		return points;
	}
}
