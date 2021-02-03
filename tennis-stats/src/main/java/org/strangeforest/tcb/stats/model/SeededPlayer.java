package org.strangeforest.tcb.stats.model;

public class SeededPlayer extends MatchPlayer {

	private final Boolean active;
	private final Integer rank;
	private final Integer bestRank;
	private final Integer eloRating;
	private String result;
	private FavoriteSurface favoriteSurface;

	public SeededPlayer(int id, String name, Integer seed, String entry, String countryId, Boolean active, Integer rank, Integer bestRank, Integer eloRating, String result) {
		super(id, name, seed, entry, countryId);
		this.active = active;
		this.rank = rank;
		this.bestRank = bestRank;
		this.eloRating = eloRating;
		this.result = result;
	}

	public Boolean getActive() {
		return active;
	}

	public Integer getRank() {
		return rank;
	}

	public Integer getBestRank() {
		return bestRank;
	}

	public Integer getEloRating() {
		return eloRating;
	}

	public String getResult() {
		return result;
	}

	public FavoriteSurface getFavoriteSurface() {
		return favoriteSurface;
	}

	public void setFavoriteSurface(FavoriteSurface favoriteSurface) {
		this.favoriteSurface = favoriteSurface;
	}
}
