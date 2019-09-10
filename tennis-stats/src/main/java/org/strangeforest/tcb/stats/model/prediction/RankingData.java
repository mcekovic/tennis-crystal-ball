package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;

public final class RankingData {

	private Integer rank;
	private Integer rankPoints;
	private Integer eloRating;
	private Integer recentEloRating;
	private Integer surfaceEloRating;
	private Integer inOutEloRating;
	private Integer setEloRating;
	private LocalDate eloDate;

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

	public Integer getRecentEloRating() {
		return recentEloRating;
	}

	public void setRecentEloRating(Integer recentEloRating) {
		this.recentEloRating = recentEloRating;
	}

	public Integer getSurfaceEloRating() {
		return surfaceEloRating;
	}

	public void setSurfaceEloRating(Integer surfaceEloRating) {
		this.surfaceEloRating = surfaceEloRating;
	}

	public Integer getInOutEloRating() {
		return inOutEloRating;
	}

	public void setInOutEloRating(Integer inOutEloRating) {
		this.inOutEloRating = inOutEloRating;
	}

	public Integer getSetEloRating() {
		return setEloRating;
	}

	public void setSetEloRating(Integer setEloRating) {
		this.setEloRating = setEloRating;
	}

	public LocalDate getEloDate() {
		return eloDate;
	}

	public void setEloDate(LocalDate eloDate) {
		this.eloDate = eloDate;
	}

	public RankingData copy() {
		RankingData copy = new RankingData();
		copy.rank = rank;
		copy.rankPoints = rankPoints;
		copy.eloRating = eloRating;
		copy.recentEloRating = recentEloRating;
		copy.surfaceEloRating = surfaceEloRating;
		copy.inOutEloRating = inOutEloRating;
		copy.setEloRating = setEloRating;
		copy.eloDate = eloDate;
		return copy;
	}
}
