package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.elo.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;

public class DominanceSeason {

	private static final double DOMINANCE_RATIO_COEFFICIENT = 1600.0;
	private static final Map<Surface, Double> SURFACE_DOMINANCE_RATIO_COEFFICIENT = Map.of(
		HARD,   450.0,
		CLAY,   140.0,
		GRASS,   70.0,
		CARPET, 120.0
	);

	public static double getDominanceRatioCoefficient(Surface surface) {
		return surface == null ? DOMINANCE_RATIO_COEFFICIENT : SURFACE_DOMINANCE_RATIO_COEFFICIENT.get(surface);
	}

	private final int season;
	private final Surface surface;
	private final boolean eligibleForEra;
	private final boolean ongoing;
	private int dominanceRatioPoints;
	private PlayerDominanceTimeline bestPlayer;
	private PlayerDominanceTimeline eraPlayer;
	private int bestPlayerPoints;
	private double predictability;
	private double eloPredictability;
	private Map<Integer, Integer> averageEloRatings;

	public DominanceSeason(int season, Surface surface) {
		this.season = season;
		this.surface = surface;
		var today = LocalDate.now();
		var year = today.getYear();
		ongoing = season == year && today.getMonth().compareTo(Month.NOVEMBER) < 0;
		eligibleForEra = season < year || !ongoing;
		averageEloRatings = new HashMap<>();
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
		return dominanceRatioPoints / getDominanceRatioCoefficient(surface);
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

	public double getPredictability() {
		return predictability;
	}

	public int getPredictabilityClass() {
		return predictabilityClass(predictability);
	}

	public void setPredictability(double predictability) {
		this.predictability = predictability;
	}

	public double getEloPredictability() {
		return eloPredictability;
	}

	public int getEloPredictabilityClass() {
		return predictabilityClass(eloPredictability);
	}

	public void setEloPredictability(double eloPredictability) {
		this.eloPredictability = eloPredictability;
	}

	private static int predictabilityClass(double predictability) {
		return 10 * min(10, (int)round((predictability - 60.0) * 5.0 / 8.0));
	}

	Map<Integer, Integer> getAverageEloRatings() {
		return averageEloRatings;
	}

	void setAverageEloRatings(Map<Integer, Integer> averageEloRatings) {
		this.averageEloRatings = averageEloRatings;
	}

	public int getAverageEloRating(int topN) {
		return averageEloRatings.getOrDefault(topN, (int)StartEloRatings.START_RATING);
	}

	public int getAverageEloRatingPoints(int topN) {
		return 10 * ((getAverageEloRating(topN) - 1700) / 100);
	}

	public void addAverageEloRating(int topN, int eloRating) {
		averageEloRatings.put(topN, eloRating);
	}

	void processPlayer(PlayerDominanceTimeline player) {
		var seasonPoints = player.getSeasonPoints(season);
		if (seasonPoints != null) {
			var playerPoints = seasonPoints.getPoints();
			dominanceRatioPoints += playerPoints * player.getGoatPoints();
			if (bestPlayer == null || playerPoints > bestPlayerPoints || (playerPoints == bestPlayerPoints && player.getGoatPoints() > bestPlayer.getGoatPoints())) {
				bestPlayer = player;
				bestPlayerPoints = playerPoints;
			}
		}
	}
}
