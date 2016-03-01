package org.strangeforest.tcb.stats.model;

public class SetScore {

	private final int wGames, lGames;
	private final Integer wTBPoints, lTBPoints;

	public SetScore(int wGames, int lGames, Integer wTBPoints, Integer lTBPoints) {
		this.wGames = wGames;
		this.lGames = lGames;
		this.wTBPoints = wTBPoints;
		this.lTBPoints = lTBPoints;
	}

	public int getwGames() {
		return wGames;
	}

	public int getlGames() {
		return lGames;
	}

	public Integer getwTBPoints() {
		return wTBPoints;
	}

	public Integer getlTBPoints() {
		return lTBPoints;
	}
}
