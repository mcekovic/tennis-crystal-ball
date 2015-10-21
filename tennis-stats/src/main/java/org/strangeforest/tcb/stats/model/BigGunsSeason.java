package org.strangeforest.tcb.stats.model;

public class BigGunsSeason {

	private final int season;
	private int dominanceRatioPoints;
	private BigGunsPlayerTimeline bestPlayer;
	private int bestPlayerPoints;

	private static final float DOMINANCE_RATIO_COEFFICIENT = 1000.0f;

	public BigGunsSeason(int season) {
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public float getDominanceRatio() {
		return dominanceRatioPoints / DOMINANCE_RATIO_COEFFICIENT;
	}

	public int getDominanceRatioRounded() {
		return 10*(Math.round(getDominanceRatio())/10);
	}

	public BigGunsPlayerTimeline getBestPlayer() {
		return bestPlayer;
	}

	public void processPlayer(BigGunsPlayerTimeline player) {
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
