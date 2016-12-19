package org.strangeforest.tcb.stats.model.prediction;

public class EloRating {

	private final Integer eloRating;
	private final Integer surfaceEloRating;

	public EloRating(Integer eloRating, Integer surfaceEloRating) {
		this.eloRating = eloRating;
		this.surfaceEloRating = surfaceEloRating;
	}

	public Integer getEloRating() {
		return eloRating;
	}

	public Integer getSurfaceEloRating() {
		return surfaceEloRating;
	}
}
