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

	public String formatted() {
		StringBuilder sb = new StringBuilder();
		sb.append(wGames).append('-').append(lGames);
		if (wTBPoints != null && lTBPoints != null)
			sb.append("<sup>(").append(wTBPoints > lTBPoints ? lTBPoints : wTBPoints).append(")</sup>");
		return sb.toString();
	}
}
