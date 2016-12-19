package org.strangeforest.tcb.stats.model.prediction;

public class MatchEloRatings {

	private final EloRating eloRatings1;
	private final EloRating eloRatings2;

	public MatchEloRatings(EloRating eloRatings1, EloRating eloRatings2) {
		this.eloRatings1 = eloRatings1;
		this.eloRatings2 = eloRatings2;
	}

	public EloRating getEloRatings1() {
		return eloRatings1;
	}

	public EloRating getEloRatings2() {
		return eloRatings2;
	}
}
