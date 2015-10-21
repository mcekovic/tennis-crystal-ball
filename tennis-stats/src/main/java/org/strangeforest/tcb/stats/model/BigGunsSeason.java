package org.strangeforest.tcb.stats.model;

public class BigGunsSeason {

	private final int season;
	private int totalPoints;
	private BigGunsPlayerTimeline bestPlayer;
	private int bestPlayerPoints;

	public BigGunsSeason(int season) {
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public BigGunsPlayerTimeline getBestPlayer() {
		return bestPlayer;
	}

	public void processPlayer(BigGunsPlayerTimeline player) {
		SeasonPoints seasonPoints = player.getSeasonPoints(season);
		if (seasonPoints != null) {
			int playerPoints = seasonPoints.getPoints();
			totalPoints += playerPoints;
			if (bestPlayer == null || playerPoints > bestPlayerPoints || (playerPoints == bestPlayerPoints && player.getGoatPoints() > bestPlayer.getGoatPoints())) {
				bestPlayer = player;
				bestPlayerPoints = playerPoints;
			}
		}
	}
}
