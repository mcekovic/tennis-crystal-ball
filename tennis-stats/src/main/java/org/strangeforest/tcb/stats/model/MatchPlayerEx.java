package org.strangeforest.tcb.stats.model;

public class MatchPlayerEx extends MatchPlayer {

	private final Integer rank;
	protected Integer eloRating;
	protected Integer nextEloRating;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating, Integer nextEloRating) {
		super(id, name, seed, entry, countryId);
		this.rank = rank;
		this.eloRating = eloRating;
		this.nextEloRating = nextEloRating;
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

	public void setEloRating(Integer eloRating) {
		this.eloRating = eloRating;
	}

	public Integer getEloRatingDelta() {
		if (nextEloRating == null)
			return null;
		int delta = nextEloRating - (eloRating != null ? eloRating : 1500);
		return delta != 0 ? delta : null;
	}
}
