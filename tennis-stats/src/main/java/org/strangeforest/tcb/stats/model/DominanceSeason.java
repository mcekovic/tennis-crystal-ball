package org.strangeforest.tcb.stats.model;

public class DominanceSeason {

	private final int season;
	private int dominanceRatioPoints;
	private PlayerDominanceTimeline bestPlayer;
	private PlayerDominanceTimeline eraPlayer;
	private int bestPlayerPoints;

	private static final double DOMINANCE_RATIO_COEFFICIENT = 1000.0;

	public DominanceSeason(int season) {
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public double getDominanceRatio() {
		return dominanceRatioPoints / DOMINANCE_RATIO_COEFFICIENT;
	}

	public int getDominanceRatioRounded() {
		return DominanceTimeline.roundDominanceRatio(getDominanceRatio());
	}

	public PlayerDominanceTimeline getBestPlayer() {
		return bestPlayer;
	}

	PlayerDominanceTimeline getEraPlayer() {
		return eraPlayer;
	}

	void setEraPlayer(PlayerDominanceTimeline eraPlayer) {
		this.eraPlayer = eraPlayer;
	}

	public void processPlayer(PlayerDominanceTimeline player) {
		SeasonPoints seasonPoints = player.getSeasonPoints(season);
		if (seasonPoints != null) {
			int playerPoints = seasonPoints.getPoints();
			dominanceRatioPoints += playerPoints * player.getGoatPoints();
			if (bestPlayer == null || playerPoints > bestPlayerPoints || (playerPoints == bestPlayerPoints && player.getGoatPoints() > bestPlayer.getGoatPoints())) {
				bestPlayer = player;
				bestPlayerPoints = playerPoints;
			}
		}
	}
}
