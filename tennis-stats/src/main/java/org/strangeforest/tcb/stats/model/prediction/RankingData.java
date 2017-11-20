package org.strangeforest.tcb.stats.model.prediction;

public final class RankingData {

	private Integer rank;
	private Integer rankPoints;
	private Integer eloRating;
	private Integer surfaceEloRating;
	private Integer outInEloRating;

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Integer getRankPoints() {
		return rankPoints;
	}

	public void setRankPoints(Integer rankPoints) {
		this.rankPoints = rankPoints;
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

	public Integer getOutInEloRating() {
		return outInEloRating;
	}

	public void setOutInEloRating(Integer outInEloRating) {
		this.outInEloRating = outInEloRating;
	}
}
