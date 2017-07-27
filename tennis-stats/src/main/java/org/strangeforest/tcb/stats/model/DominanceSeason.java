package org.strangeforest.tcb.stats.model;

import java.time.*;

public class DominanceSeason {

	private final int season;
	private final boolean eligibleForEra;
	private final boolean ongoing;
	private int dominanceRatioPoints;
	private PlayerDominanceTimeline bestPlayer;
	private PlayerDominanceTimeline eraPlayer;
	private int bestPlayerPoints;

	public static final double DOMINANCE_RATIO_COEFFICIENT = 1500.0;

	public DominanceSeason(int season) {
		this.season = season;
		LocalDate today = LocalDate.now();
		int year = today.getYear();
		ongoing = season == year && today.getMonth().compareTo(Month.NOVEMBER) < 0;
		eligibleForEra = season < year || !ongoing;
	}

	public int getSeason() {
		return season;
	}

	public boolean isEligibleForEra() {
		return eligibleForEra;
	}

	public boolean isOngoingSeason() {
		return ongoing;
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

	void processPlayer(PlayerDominanceTimeline player) {
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
