package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.core.*;

public class EloRatingDelta {

	private final Integer eloRating;
	private Integer nextEloRating;

	public EloRatingDelta(Integer eloRating, Integer nextEloRating) {
		this.eloRating = eloRating;
		this.nextEloRating = nextEloRating;
	}

	public EloRatingDelta(EloRatingDelta eloRatingDelta) {
		eloRating = eloRatingDelta.eloRating;
		nextEloRating = eloRatingDelta.nextEloRating;
	}

	public Integer getEloRating() {
		return eloRating;
	}

	public int getNextEloRating() {
		return ensureEloRating(nextEloRating);
	}

	public Integer getEloRatingDelta() {
		if (nextEloRating == null)
			return null;
		int delta = nextEloRating - ensureEloRating(eloRating);
		return delta != 0 ? delta : null;
	}

	void setNextEloRating(Integer nextEloRating) {
		this.nextEloRating = nextEloRating;
	}

	private static int ensureEloRating(Integer eloRating) {
		return eloRating != null ? eloRating : Player.START_ELO_RATING;
	}
}
