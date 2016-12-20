package org.strangeforest.tcb.stats.model.prediction;

public final class MatchEloRatings {

	private final EloRating eloRating1;
	private final EloRating eloRating2;

	public MatchEloRatings(EloRating eloRating1, EloRating eloRating2) {
		this.eloRating1 = eloRating1;
		this.eloRating2 = eloRating2;
	}

	public EloRating getEloRating1() {
		return eloRating1;
	}

	public EloRating getEloRating2() {
		return eloRating2;
	}
}
