package org.strangeforest.tcb.stats.model.elo;

import java.util.*;

import static java.util.Arrays.*;

public abstract class StartEloRatings {

	public static final double START_RATING = 1500.0;

	static final int START_RATING_RANK = 1000;
	private static final List<RatingPoint> START_RATING_TABLE = asList(
		new RatingPoint(  1, 2394),
		new RatingPoint(  2, 2323),
		new RatingPoint(  3, 2275),
		new RatingPoint(  4, 2237),
		new RatingPoint(  5, 2204),
		new RatingPoint(  7, 2160),
		new RatingPoint( 10, 2117),
		new RatingPoint( 15, 2070),
		new RatingPoint( 20, 2035),
		new RatingPoint( 30, 1978),
		new RatingPoint( 50, 1907),
		new RatingPoint( 70, 1856),
		new RatingPoint(100, 1801),
		new RatingPoint(150, 1730),
		new RatingPoint(200, 1658),
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
			for (RatingPoint point : START_RATING_TABLE) {
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
