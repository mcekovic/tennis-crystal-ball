package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

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

	public int eloRating() {
		return eloRating != null ? eloRating : Player.START_ELO_RATING;
	}

	public void setEloRating(Integer eloRating) {
		this.eloRating = eloRating;
	}

	public Integer getEloRatingDelta() {
		if (nextEloRating == null)
			return null;
		int delta = nextEloRating - eloRating();
		return delta != 0 ? delta : null;
	}
}
