package org.strangeforest.tcb.stats.model.prediction;

public final class RankingData {

	private Integer rank;
	private Integer eloRating;
	private Integer surfaceEloRating;

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Integer getEloRating() {
		return eloRating;
	}

	public void setEloRating(Integer eloRating) {
		this.eloRating = eloRating;
	}

	public Integer getSurfaceEloRating() {
		return surfaceEloRating;
	}

	public void setSurfaceEloRating(Integer surfaceEloRating) {
		this.surfaceEloRating = surfaceEloRating;
	}
}
