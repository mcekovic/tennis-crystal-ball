package org.strangeforest.tcb.stats.model;

public class FavoritePlayer extends PlayerRow {

	private double probability;

	public FavoritePlayer(int favorite, int playerId, String name, String countryId, double probability) {
		super(favorite, playerId, name, countryId, null);
		this.probability = probability;
	}

	public double getProbability() {
		return probability;
	}
}
