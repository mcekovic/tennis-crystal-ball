package org.strangeforest.tcb.stats.model.elo;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.elo.StartEloRatings.*;

public abstract class EloCalculator {

	private static final double RATING_SCALE = 400.0;
	private static final double K_FACTOR = 32.0;
	private static final double K_FUNCTION_AMPLIFIER = 10.0;
	private static final double K_FUNCTION_AMPLIFIER_GRADIENT = 63.0;
	private static final double K_FUNCTION_MULTIPLIER = 2.0 * (K_FUNCTION_AMPLIFIER - 1.0);
	private static final double DELTA_RATING_CAP = 200.0;

	private static final double RECENT_K_FACTOR = 2.0;
	private static final double SET_K_FACTOR = 0.5;
	private static final double GAME_K_FACTOR = 0.0556;
	private static final double SERVICE_GAME_K_FACTOR = 0.1667;
	private static final double RETURN_GAME_K_FACTOR = 0.1667;
	private static final double TIE_BREAK_K_FACTOR = 1.5;
	
	private static final double INACTIVITY_ADJ_RATING_FACTOR = 5.0;
	private static final int INACTIVITY_ADJ_PERIOD = 500;
	static final int INACTIVITY_ADJ_NO_PENALTY_PERIOD = 30;
	static final int INACTIVITY_RESET_PERIOD = INACTIVITY_ADJ_PERIOD * 4;
	private static final double INACTIVITY_ADJ_GRADIENT = 100.0;
	private static final double INACTIVITY_ADJ_DRIFT = 1.0 / (1.0 + pow(E, (INACTIVITY_ADJ_PERIOD - INACTIVITY_ADJ_NO_PENALTY_PERIOD) / INACTIVITY_ADJ_GRADIENT));
	private static final double DEFAULT_INACTIVITY_ADJ_FACTOR = 1.0;
	private static final Map<String, Double> INACTIVITY_ADJ_FACTOR = Map.of("R", 2.0, "H", 0.85, "C", 0.75, "G", 0.5, "P", 0.5, "O", 0.85, "I", 0.6);

	private static final LocalDate FROZEN_BEGIN = LocalDate.of(2020, 3, 23);
	private static final LocalDate FROZEN_END = LocalDate.of(2020, 8, 24);

//	public static volatile double tuningValue;

	/**
	 * Tennis-customized Elo K-Factor depending on tournament level, round and best of
	 * @return Elo K-Factor
	 */
	public static double kFactor(String level, String round, short bestOf, String outcome) {
		var kFactor = K_FACTOR;
		switch (level) {
			case "G": break;
			case "F": kFactor *= 0.90; break;
			case "L": kFactor *= 0.85; break;
			case "M": kFactor *= 0.85; break;
			case "O": kFactor *= 0.80; break;
			case "A": kFactor *= 0.75; break;
			case "B":
			case "D":
			case "T": kFactor *= 0.70; break;
			default: kFactor *= 0.65; break;
		}
		switch (round) {
			case "F": break;
			case "BR": kFactor *= 0.95; break;
			case "SF": kFactor *= 0.90; break;
			case "QF": kFactor *= 0.85; break;
			case "R16": kFactor *= 0.80; break;
			case "R32": kFactor *= 0.80; break;
			case "R64": kFactor *= 0.75; break;
			case "R128": kFactor *= 0.75; break;
			case "RR": kFactor *= 0.85; break;
		}
		if (bestOf < 5)
			kFactor *= 0.90;
		if (Objects.equals(outcome, "W/O"))
			kFactor *= 0.50;
		return kFactor;
	}

	/**
	 * K-Function returns values from 1 to 10 depending on current rating.
	 * It stabilizes ratings at the top, while allows fast progress of lower rated players.
	 * For high ratings returns 1
	 * For low ratings return 10
	 * @return values from 1 to 10, depending on current rating
	 */
	public static double kFunction(double rating, String type) {
		return 1.0 + K_FUNCTION_MULTIPLIER / (1.0 + pow(2.0, (ratingFromType(rating, type) - START_RATING) / K_FUNCTION_AMPLIFIER_GRADIENT));
	}
	
	public static double kFunction(double rating) {
		return kFunction(rating, "E");
	}

	public static double deltaRating(double winnerRating, double loserRating, String level, String round, short bestOf, String outcome) {
		if (Objects.equals(outcome, "ABD"))
			return 0.0;
		var delta = 1.0 / (1.0 + pow(10.0, (winnerRating - loserRating) / RATING_SCALE));
		return kFactor(level, round, bestOf, outcome) * delta;
	}

	public static double deltaRating(EloSurfaceFactors eloSurfaceFactors, double winnerRating, double loserRating, MatchForElo match, String type) {
		var level = match.level;
		var round = match.round;
		var bestOf = Character.isLowerCase(type.charAt(0)) ? (short)5 : match.bestOf;
		var outcome = match.outcome;
		var delta = deltaRating(winnerRating, loserRating, level, round, bestOf, outcome);
		switch (type) {
			case "E": return delta;
			case "R": return RECENT_K_FACTOR * delta;
			case "H": case "C": case "G": case "P":
			case "O": case "I": return eloSurfaceFactors.surfaceKFactor(type, match.endDate.getYear()) * delta;
			default: {
				var wDelta = delta;
				var lDelta = deltaRating(loserRating, winnerRating, level, round, bestOf, outcome);
				switch (type) {
					case "s": return SET_K_FACTOR * (wDelta * match.wSets - lDelta * match.lSets);
					case "g": return GAME_K_FACTOR * (wDelta * match.wGames - lDelta * match.lGames);
					case "sg": return SERVICE_GAME_K_FACTOR * (wDelta * match.wSvGms * returnToServeRatio(match.surface) - lDelta * match.lRtGms);
					case "rg": return RETURN_GAME_K_FACTOR * (wDelta * match.wRtGms - lDelta * match.lSvGms * returnToServeRatio(match.surface));
					case "tb": {
						var wTBs = match.wTbs;
						var lTBs = match.lTbs;
						if (lTBs > wTBs) {
							var d = wDelta; wDelta = lDelta; lDelta = d;
						}
						return TIE_BREAK_K_FACTOR * (wDelta * wTBs - lDelta * lTBs);
					}
//					case "X": {
//						int wTBs = match.wTbs;
//						int lTBs = match.lTbs;
//						if (lTBs > wTBs) {
//							double d = wDelta; wDelta = lDelta; lDelta = d;
//						}
//						return
//							0.59 * delta +
//							0.19 * (wDelta * match.wSets - lDelta * match.lSets) +
//							0.002 * (wDelta * match.wGames - lDelta * match.lGames) +
//							0.055 * (wDelta * wTBs - lDelta * lTBs);
//					}
					default: throw new IllegalStateException();
				}
			}
		}
	}

	public static double newRating(double rating, double delta, String type) {
		return rating + capDeltaRating(delta * kFunction(rating, type), type);
	}

	private static double capDeltaRating(double delta, String type) {
		return signum(delta) * min(abs(delta), ratingDiffForType(DELTA_RATING_CAP, type));
	}

	/**
	 * Adjusts rating after period of inactivity using logistic function
 	 */
	public static double adjustRating(double rating, int daysSinceLastMatch, String type) {
		var adjustmentFactor = inactivityAdjustmentFactor(type);
		var maxAdjustment = ratingDiffForType((rating - START_RATING) / INACTIVITY_ADJ_RATING_FACTOR, type);
		var adjustment = adjustmentFactor * maxAdjustment * (1.0 / (1.0 + pow(E, (INACTIVITY_ADJ_PERIOD - adjustmentFactor * daysSinceLastMatch) / INACTIVITY_ADJ_GRADIENT)) - INACTIVITY_ADJ_DRIFT) / (1 - INACTIVITY_ADJ_DRIFT);
		return max(START_RATING, rating - adjustment);
	}

	private static double inactivityAdjustmentFactor(String type) {
		return INACTIVITY_ADJ_FACTOR.getOrDefault(type, DEFAULT_INACTIVITY_ADJ_FACTOR);
	}

	static double returnToServeRatio(String surface) {
		if (surface == null)
			return 0.297;
		switch (surface) {
			case "H": return 0.281;
			case "C": return 0.365;
			case "G": return 0.227;
			case "P": return 0.243;
			default: throw new IllegalStateException();
		}
	}

	static double ratingForType(double rating, String type) {
		var factor = ratingTypeFactor(type);
		return factor != null ? START_RATING + (rating - START_RATING) * factor : rating;
	}

	static double ratingFromType(double typeRating, String type) {
		var factor = ratingTypeFactor(type);
		return factor != null ? START_RATING + (typeRating - START_RATING) / factor : typeRating;
	}

	static double ratingDiffForType(double ratingDiff, String type) {
		var factor = ratingTypeFactor(type);
		return factor != null ? ratingDiff * factor : ratingDiff;
	}

	private static Double ratingTypeFactor(String type) {
		switch (type) {
			case "R": return 1.1;
			case "s": return 0.8;
			case "g": return 0.25;
			case "sg": return 0.4;
			case "rg": return 0.4;
			case "tb": return 0.4;
			default: return null;
		}
	}

	public static double eloWinProbability(double eloRating1, double eloRating2) {
		return 1.0 / (1.0 + pow(10.0, (eloRating2 - eloRating1) / RATING_SCALE));
	}

	public static int daysBetween(LocalDate from, LocalDate to) {
		if (from.isBefore(FROZEN_BEGIN)) {
			if (to.isAfter(FROZEN_BEGIN)) {
				if (to.isAfter(FROZEN_END))
					return rawDaysBetween(from, FROZEN_BEGIN) + rawDaysBetween(FROZEN_END, to);
				else
					return rawDaysBetween(from, FROZEN_BEGIN);
			}
			else
				return rawDaysBetween(from, to);
		}
		else if (from.isBefore(FROZEN_END)) {
			if (to.isAfter(FROZEN_END))
				return rawDaysBetween(FROZEN_END, to);
			else
				return 0;
		}
		else
			return rawDaysBetween(from, to);
	}

	private static int rawDaysBetween(LocalDate from, LocalDate to) {
		return (int)ChronoUnit.DAYS.between(from, to);
	}
}
