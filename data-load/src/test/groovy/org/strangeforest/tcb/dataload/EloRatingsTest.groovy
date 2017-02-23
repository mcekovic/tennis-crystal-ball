package org.strangeforest.tcb.dataload

import org.junit.*

import static org.strangeforest.tcb.dataload.EloRatings.*

class EloRatingsTest {

	@Test
	void "Start rating is calculated correctly"() {
		assert startRating(  1) == 2368
		assert startRating(  2) == 2288
		assert startRating(  3) == 2234
		assert startRating(  4) == 2191
		assert startRating(  5) == 2157
		assert startRating(  6) == 2132
		assert startRating(  7) == 2108
		assert startRating(  8) == 2092
		assert startRating(  9) == 2077
		assert startRating( 10) == 2062
		assert startRating( 40) == 1879
		assert startRating(125) == 1648
		assert startRating(250) == 1516
		assert startRating(500) == 1500
	}

	static startRating(int rank) {
		new EloRating(100, rank, null).rating
	}
}