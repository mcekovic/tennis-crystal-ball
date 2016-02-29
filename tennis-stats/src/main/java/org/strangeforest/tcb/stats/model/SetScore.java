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

	public String getWinnerScore() {
		return score(wGames, wTBPoints);
	}

	public String getLoserScore() {
		return score(lGames, lTBPoints);
	}

	private static String score(int wGames, Integer wTBPoints) {
		return wTBPoints != null ? String.valueOf(wGames) + "<sup>(" + wTBPoints + ")</sup>" : String.valueOf(wGames);
	}
}
