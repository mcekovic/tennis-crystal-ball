package org.strangeforest.tcb.dataload

class StartEloRatings {

	static final int START_RATING = 1500
	static final int START_RATING_RANK = 300
	static final Map<String, List<RatingPoint>> START_RATING_TABLE = [
		(null): [
			ratingPoint(  1, 2368),
			ratingPoint(  2, 2288),
			ratingPoint(  3, 2234),
			ratingPoint(  4, 2191),
			ratingPoint(  5, 2157),
			ratingPoint(  7, 2108),
			ratingPoint( 10, 2062),
			ratingPoint( 15, 2014),
			ratingPoint( 20, 1979),
			ratingPoint( 30, 1922),
			ratingPoint( 50, 1837),
			ratingPoint( 70, 1767),
			ratingPoint(100, 1691),
			ratingPoint(150, 1605),
			ratingPoint(200, 1533),
			ratingPoint(START_RATING_RANK, START_RATING)
		],
		H: [
			ratingPoint(  1, 2327),
			ratingPoint(  2, 2246),
			ratingPoint(  3, 2206),
			ratingPoint(  4, 2162),
			ratingPoint(  5, 2112),
			ratingPoint(  7, 2069),
			ratingPoint( 10, 2021),
			ratingPoint( 15, 1970),
			ratingPoint( 20, 1941),
			ratingPoint( 30, 1879),
			ratingPoint( 50, 1788),
			ratingPoint( 70, 1724),
			ratingPoint(100, 1669),
			ratingPoint(150, 1598),
			ratingPoint(200, 1539),
			ratingPoint(START_RATING_RANK, START_RATING)
		],
		C: [
			ratingPoint(  1, 2320),
			ratingPoint(  2, 2246),
			ratingPoint(  3, 2172),
			ratingPoint(  4, 2142),
			ratingPoint(  5, 2138),
			ratingPoint(  7, 2094),
			ratingPoint( 10, 2034),
			ratingPoint( 15, 1979),
			ratingPoint( 20, 1936),
			ratingPoint( 30, 1877),
			ratingPoint( 50, 1802),
			ratingPoint( 70, 1749),
			ratingPoint(100, 1681),
			ratingPoint(150, 1613),
			ratingPoint(200, 1549),
			ratingPoint(START_RATING_RANK, START_RATING)
		],
		G: [
			ratingPoint(  1, 2399),
			ratingPoint(  2, 2277),
			ratingPoint(  3, 2234),
			ratingPoint(  4, 2184),
			ratingPoint(  5, 2115),
			ratingPoint(  7, 2063),
			ratingPoint( 10, 2015),
			ratingPoint( 15, 1973),
			ratingPoint( 20, 1941),
			ratingPoint( 30, 1895),
			ratingPoint( 50, 1819),
			ratingPoint( 70, 1755),
			ratingPoint(100, 1717),
			ratingPoint(150, 1644),
			ratingPoint(200, 1573),
			ratingPoint(START_RATING_RANK, START_RATING)
		],
		P: [
			ratingPoint(  1, 2293),
			ratingPoint(  2, 2223),
			ratingPoint(  3, 2151),
			ratingPoint(  4, 2103),
			ratingPoint(  5, 2067),
			ratingPoint(  7, 2013),
			ratingPoint( 10, 1967),
			ratingPoint( 15, 1926),
			ratingPoint( 20, 1900),
			ratingPoint( 30, 1828),
			ratingPoint( 50, 1762),
			ratingPoint( 70, 1703),
			ratingPoint(100, 1655),
			ratingPoint(150, 1598),
			ratingPoint(200, 1551),
			ratingPoint(START_RATING_RANK, START_RATING)
		]
	]

	static int startRating(Integer rank, String surface = null) {
		if (rank) {
			RatingPoint prevPoint
			for (RatingPoint point : START_RATING_TABLE[surface]) {
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
		int rank;
		int eloRating;
	}
}
