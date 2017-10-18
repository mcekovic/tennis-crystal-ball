package org.strangeforest.tcb.stats.model;

public class MatchPlayerEx extends MatchPlayer {

	private final Integer rank;
	protected Integer eloRating;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating) {
		super(id, name, seed, entry, countryId);
		this.rank = rank;
		this.eloRating = eloRating;
	}

	public MatchPlayerEx(MatchPlayerEx player) {
		super(player);
		rank = player.rank;
		eloRating = player.eloRating;
	}

	public Integer getRank() {
		return rank;
	}

	public Integer getEloRating() {
		return eloRating;
	}

	void setEloRating(Integer eloRating) {
		this.eloRating = eloRating;
	}
}
