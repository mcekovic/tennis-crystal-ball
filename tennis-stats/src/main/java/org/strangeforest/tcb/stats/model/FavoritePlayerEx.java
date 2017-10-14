package org.strangeforest.tcb.stats.model;

public class FavoritePlayerEx extends FavoritePlayer {

	private final Integer currentRank;
	private final Integer bestRank;
	private Integer eloRating;
	private final Integer surfaceEloRating;
	private WonLost last52WeeksWonLost;
	private WonLost last52WeeksSurfaceWonLost;
	private final Integer last52WeeksTitles;
	private final Integer age;

	public FavoritePlayerEx(int favorite, int playerId, String name, String countryId, double probability, Integer currentRank, Integer bestRank, Integer eloRating, Integer surfaceEloRating, Integer last52WeeksTitles, Integer age) {
		super(favorite, playerId, name, countryId, probability);
		this.currentRank = currentRank;
		this.bestRank = bestRank;
		this.eloRating = eloRating;
		this.surfaceEloRating = surfaceEloRating;
		this.last52WeeksTitles = last52WeeksTitles;
		this.age = age;
	}

	public Integer getCurrentRank() {
		return currentRank;
	}

	public Integer getBestRank() {
		return bestRank;
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

	public WonLost getLast52WeeksWonLost() {
		return last52WeeksWonLost;
	}

	public void setLast52WeeksWonLost(WonLost last52WeeksWonLost) {
		this.last52WeeksWonLost = last52WeeksWonLost;
	}

	public WonLost getLast52WeeksSurfaceWonLost() {
		return last52WeeksSurfaceWonLost;
	}

	public void setLast52WeeksSurfaceWonLost(WonLost last52WeeksSurfaceWonLost) {
		this.last52WeeksSurfaceWonLost = last52WeeksSurfaceWonLost;
	}

	public Integer getLast52WeeksTitles() {
		return last52WeeksTitles;
	}

	public Integer getAge() {
		return age;
	}
}
