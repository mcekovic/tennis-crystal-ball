package org.strangeforest.tcb.stats.model.elo;

import java.util.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.elo.StartEloRatings.*;

public abstract class EloCalculator {

	private static final double RECENT_K_FACTOR = 2.0;
	private static final double SET_K_FACTOR = 0.5;
	private static final double GAME_K_FACTOR = 0.0556;
	private static final double SERVICE_GAME_K_FACTOR = 0.1667;
	private static final double RETURN_GAME_K_FACTOR = 0.1667;
	private static final double TIE_BREAK_K_FACTOR = 1.5;
	private static final double ADJUSTMENT_FACTOR = 6.0;

//	public static volatile double tuningValue;

	public static double kFactor(String level, String round, short bestOf, String outcome) {
		double kFactor = 32.0;
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
		return 1.0 + 18.0 / (1.0 + pow(2.0, (ratingFromType(rating, type) - START_RATING) / 63.0));
	}
	
	public static double kFunction(double rating) {
		return kFunction(rating, "E");
	}

	public static double deltaRating(double winnerRating, double loserRating, String level, String round, short bestOf, String outcome) {
		if (Objects.equals(outcome, "ABD"))
			return 0.0;
		double delta = 1.0 / (1.0 + pow(10.0, (winnerRating - loserRating) / 400.0));
		return kFactor(level, round, bestOf, outcome) * delta;
	}

	public static double deltaRating(EloSurfaceFactors eloSurfaceFactors, double winnerRating, double loserRating, MatchForElo match, String type, boolean forSurface) {
		String level = match.level;
		String round = match.round;
		short bestOf = Character.isLowerCase(type.charAt(0)) ? (short)5 : match.bestOf;
		String outcome = match.outcome;
		double delta = deltaRating(winnerRating, loserRating, level, round, bestOf, outcome);
		if (type.equals("E"))
			return delta;
		else if (forSurface)
			return delta * eloSurfaceFactors.surfaceKFactor(type, match.endDate.getYear());
		else {
			if (type.equals("R"))
				return RECENT_K_FACTOR * delta;
			double wDelta = delta;
			double lDelta = deltaRating(loserRating, winnerRating, level, round, bestOf, outcome);
			switch (type) {
				case "s": return SET_K_FACTOR * (wDelta * match.wSets - lDelta * match.lSets);
				case "g": return GAME_K_FACTOR * (wDelta * match.wGames - lDelta * match.lGames);
				case "sg": return SERVICE_GAME_K_FACTOR * (wDelta * match.wSvGms * returnToServeRatio(match.surface) - lDelta * match.lRtGms);
				case "rg": return RETURN_GAME_K_FACTOR * (wDelta * match.wRtGms - lDelta * match.lSvGms * returnToServeRatio(match.surface));
				case "tb": {
					int wTBs = match.wTbs;
					int lTBs = match.lTbs;
					if (lTBs > wTBs) {
						double d = wDelta; wDelta = lDelta; lDelta = d;
					}
					return TIE_BREAK_K_FACTOR * (wDelta * wTBs - lDelta * lTBs);
				}
				default: throw new IllegalStateException();
			}
		}
	}

	public static double newRating(double rating, double delta, String type) {
		return rating + capDeltaRating(delta * kFunction(rating, type), type);
	}

	private static double capDeltaRating(double delta, String type) {
		return signum(delta) * min(abs(delta), ratingDiffForType(200.0, type));
	}

	/**
	 * Adjusts rating after period of inactivity
 	 */
	public static double adjustRating(double rating, int daysSinceLastMatch, int adjustmentPeriod, double adjustmentPeriodFactor, String type) {
		double maxAdjustment = ratingDiffForType((rating - START_RATING) / ADJUSTMENT_FACTOR, type);
		double drift = 1.0 / (1.0 + pow(E, adjustmentPeriod / adjustmentPeriodFactor));
		double adjustment = (maxAdjustment + drift) * (1.0 / (1.0 + pow(E, (adjustmentPeriod - daysSinceLastMatch) / adjustmentPeriodFactor)) - drift);
		return max(START_RATING, rating - adjustment);
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
		Double factor = ratingTypeFactor(type);
		return factor != null ? START_RATING + (rating - START_RATING) * factor : rating;
	}

	static double ratingFromType(double typeRating, String type) {
		Double factor = ratingTypeFactor(type);
		return factor != null ? START_RATING + (typeRating - START_RATING) / factor : typeRating;
	}

	static double ratingDiffForType(double ratingDiff, String type) {
		Double factor = ratingTypeFactor(type);
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
}
