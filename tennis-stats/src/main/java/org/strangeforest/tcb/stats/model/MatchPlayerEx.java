package org.strangeforest.tcb.stats.model;

public class MatchPlayerEx extends MatchPlayer {

	private Integer rank;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId, Integer rank) {
		super(id, name, seed, entry, countryId);
		this.rank = rank;
	}

	public Integer getRank() {
		return rank;
	}
}
