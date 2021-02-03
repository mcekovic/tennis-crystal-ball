package org.strangeforest.tcb.stats.model.elo;

import java.util.*;

public abstract class StartEloRatings {

	public static final double START_RATING = 1500.0;

	static final int START_RATING_RANK = 1000;
	private static final List<RatingPoint> START_RATING_TABLE = List.of(
		new RatingPoint(  1, 2405),
		new RatingPoint(  2, 2336),
		new RatingPoint(  3, 2285),
		new RatingPoint(  4, 2246),
		new RatingPoint(  5, 2213),
		new RatingPoint(  7, 2178),
		new RatingPoint( 10, 2140),
		new RatingPoint( 15, 2097),
		new RatingPoint( 20, 2058),
		new RatingPoint( 30, 2011),
		new RatingPoint( 50, 1947),
		new RatingPoint( 70, 1889),
		new RatingPoint(100, 1836),
		new RatingPoint(150, 1776),
		new RatingPoint(200, 1714),
		new RatingPoint(START_RATING_RANK, START_RATING)
	);

	private static final class RatingPoint {
		
		int rank;
		double eloRating;

		RatingPoint(int rank, double eloRating) {
			this.rank = rank;
			this.eloRating = eloRating;
		}
	}

	public static double startRating(Integer rank) {
		if (rank != null) {
			RatingPoint prevPoint = null;
			for (var point : START_RATING_TABLE) {
				if (rank == point.rank)
					return point.eloRating;
				else if (rank < point.rank) {
					if (prevPoint != null)
						return prevPoint.eloRating - ((prevPoint.eloRating - point.eloRating) * (rank - prevPoint.rank)) / (point.rank - prevPoint.rank);
					else
						return point.eloRating;
				}
				prevPoint = point;
			}
		}
		return START_RATING;
	}
}
