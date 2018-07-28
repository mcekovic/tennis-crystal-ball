package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.core.*;

public class EloRatingDelta {

	private Integer eloRating;
	private Integer nextEloRating;

	public EloRatingDelta() {}

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
		return nextEloRating != null ? nextEloRating : Player.START_ELO_RATING;
	}

	public Integer getEloRatingDelta() {
		if (nextEloRating == null)
			return null;
		int delta = nextEloRating - eloRating();
		return delta != 0 ? delta : null;
	}

	int eloRating() {
		return eloRating != null ? eloRating : Player.START_ELO_RATING;
	}

	void setEloRatings(Integer eloRating, Integer nextEloRating) {
		if (this.eloRating == null)
			this.eloRating = eloRating;
		this.nextEloRating = nextEloRating;
	}
}
