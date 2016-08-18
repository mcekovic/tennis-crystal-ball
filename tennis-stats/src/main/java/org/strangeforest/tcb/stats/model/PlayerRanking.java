package org.strangeforest.tcb.stats.model;

public class PlayerRanking extends PlayerRow {

	private final int points;

	public PlayerRanking(int rank, int playerId, String name, String countryId, Boolean active, int points) {
		super(rank, playerId, name, countryId, active);
		this.points = points;
	}

	public int getPoints() {
		return points;
	}
}
