package org.strangeforest.tcb.stats.model;

public class FavouritePlayer extends PlayerRow {

	private double probability;

	public FavouritePlayer(int favourite, int playerId, String name, String countryId, double probability) {
		super(favourite, playerId, name, countryId, null);
		this.probability = probability;
	}

	public double getProbability() {
		return probability;
	}
}
