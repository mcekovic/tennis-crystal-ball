package org.strangeforest.tcb.stats.model;

public class MatchPlayerEx extends MatchPlayer {

	private Integer rank;
	private Integer eloRating;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating) {
		super(id, name, seed, entry, countryId);
		this.rank = rank;
		this.eloRating = eloRating;
	}

	public Integer getRank() {
		return rank;
	}

	public Integer getEloRating() {
		return eloRating;
	}
}
