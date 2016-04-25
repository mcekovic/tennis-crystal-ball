package org.strangeforest.tcb.dataload;

import org.junit.*

import static org.strangeforest.tcb.dataload.EloRatings.*;

public class EloRatingsTest {

	@Test
	void "Start rating is calculated correctly"() {
		assert rankHasStartRating(1, 2365)
		assert rankHasStartRating(2, 2290)
		assert rankHasStartRating(3, 2235)
		assert rankHasStartRating(4, 2195)
		assert rankHasStartRating(5, 2160)
		assert rankHasStartRating(6, 2135)
		assert rankHasStartRating(7, 2110)
		assert rankHasStartRating(8, 2093)
		assert rankHasStartRating(9, 2076)
		assert rankHasStartRating(10, 2060);
		assert rankHasStartRating(40, 1882);
		assert rankHasStartRating(125, 1655);
		assert rankHasStartRating(250, 1527);
		assert rankHasStartRating(500, 1500);
	}

	static def rankHasStartRating(int rank, int startRating) {
		new EloRating(rank).rating == startRating
	}
}