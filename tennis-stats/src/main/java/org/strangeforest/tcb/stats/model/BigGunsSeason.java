package org.strangeforest.tcb.stats.model;

public class BigGunsSeason {

	private final int season;
	private int dominanceRatioPoints;
	private BigGunsPlayerTimeline bestPlayer;
	private BigGunsPlayerTimeline eraPlayer;
	private int bestPlayerPoints;

	private static final double DOMINANCE_RATIO_COEFFICIENT = 1000.0;

	public BigGunsSeason(int season) {
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public double getDominanceRatio() {
		return dominanceRatioPoints / DOMINANCE_RATIO_COEFFICIENT;
	}

	public int getDominanceRatioRounded() {
		return (int)(10L*(Math.round(getDominanceRatio())/10L));
	}

	public BigGunsPlayerTimeline getBestPlayer() {
		return bestPlayer;
	}

	BigGunsPlayerTimeline getEraPlayer() {
		return eraPlayer;
	}

	void setEraPlayer(BigGunsPlayerTimeline eraPlayer) {
		this.eraPlayer = eraPlayer;
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
