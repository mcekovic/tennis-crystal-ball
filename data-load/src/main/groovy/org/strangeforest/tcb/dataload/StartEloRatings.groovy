package org.strangeforest.tcb.dataload

class StartEloRatings {

	static final int START_RATING = 1500
	static final int START_RATING_RANK = 300
	static final List<RatingPoint> START_RATING_TABLE = [
		ratingPoint(  1, 2368),
		ratingPoint(  2, 2290),
		ratingPoint(  3, 2236),
		ratingPoint(  4, 2192),
		ratingPoint(  5, 2158),
		ratingPoint(  7, 2130),
		ratingPoint( 10, 2064),
		ratingPoint( 15, 2016),
		ratingPoint( 20, 1982),
		ratingPoint( 30, 1924),
		ratingPoint( 50, 1837),
		ratingPoint( 70, 1768),
		ratingPoint(100, 1692),
		ratingPoint(150, 1605),
		ratingPoint(200, 1533),
		ratingPoint(START_RATING_RANK, START_RATING)
	]

	static int startRating(Integer rank) {
		if (rank) {
			RatingPoint prevPoint
			for (RatingPoint point : START_RATING_TABLE) {
				if (rank == point.rank)
					return point.eloRating
				else if (rank < point.rank) {
					if (prevPoint != null)
						return prevPoint.eloRating - (prevPoint.eloRating - point.eloRating) * (rank - prevPoint.rank) / (point.rank - prevPoint.rank)
					else
						return point.eloRating
				}
				prevPoint = point
			}
		}
		START_RATING
	}

	private static ratingPoint(int rank, int eloRating) {
		return new RatingPoint(rank: rank, eloRating: eloRating)
	}

	private static class RatingPoint {
		int rank
		int eloRating
	}
}
