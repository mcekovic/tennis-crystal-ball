package org.strangeforest.tcb.dataload

@Deprecated
class StartEloRatingsOld {

	static final int START_RATING = 1500
	static final int START_RATING_RANK = 1000
	static final List<RatingPoint> START_RATING_TABLE = [
		ratingPoint(  1, 2394),
		ratingPoint(  2, 2323),
		ratingPoint(  3, 2275),
		ratingPoint(  4, 2237),
		ratingPoint(  5, 2204),
		ratingPoint(  7, 2160),
		ratingPoint( 10, 2117),
		ratingPoint( 15, 2070),
		ratingPoint( 20, 2035),
		ratingPoint( 30, 1978),
		ratingPoint( 50, 1907),
		ratingPoint( 70, 1856),
		ratingPoint(100, 1801),
		ratingPoint(150, 1730),
		ratingPoint(200, 1658),
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
						return prevPoint.eloRating - ((prevPoint.eloRating - point.eloRating) * (rank - prevPoint.rank)) / (point.rank - prevPoint.rank)
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
