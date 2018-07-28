package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.forecast.*;

public class MatchPlayerEx extends MatchPlayer {

	private final Integer rank;
	protected final EloRatingDelta eloRatingDelta;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating, Integer nextEloRating) {
		super(id, name, seed, entry, countryId);
		this.rank = rank;
		eloRatingDelta = new EloRatingDelta(eloRating, nextEloRating);
	}

	public MatchPlayerEx(MatchPlayerEx player) {
		super(player);
		rank = player.rank;
		eloRatingDelta = new EloRatingDelta(player.eloRatingDelta);
	}

	public Integer getRank() {
		return rank;
	}

	public Integer getEloRating() {
		return eloRatingDelta.getEloRating();
	}

	public Integer getEloRatingDelta() {
		return eloRatingDelta.getEloRatingDelta();
	}
}
