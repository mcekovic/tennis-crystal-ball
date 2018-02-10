package org.strangeforest.tcb.dataload

import org.assertj.core.data.*
import org.junit.*

import static org.assertj.core.api.Assertions.*
import static org.strangeforest.tcb.dataload.EloRatings.*

class EloRatingsTest {

	@Test
	void "Start rating is calculated correctly"() {
		assert startRating(  1) == 2367
		assert startRating(  2) == 2290
		assert startRating(  3) == 2235
		assert startRating(  4) == 2192
		assert startRating(  5) == 2158
		assert startRating(  6) == 2134
		assert startRating(  7) == 2110
		assert startRating(  8) == 2094
		assert startRating(  9) == 2079
		assert startRating( 10) == 2064
		assert startRating( 40) == 1880
		assert startRating(125) == 1648
		assert startRating(250) == 1516
		assert startRating(500) == 1500
	}

	static startRating(int rank) {
		new EloRating(100, rank).rating
	}

	@Test
	void "kFactor is determined correctly"() {
		def offset = Offset.offset(0.000001d)
		
		assertThat(kFactor('G', 'F', (short)5, null)).isCloseTo(100.0d, offset)
		assertThat(kFactor('G', 'SF', (short)5, null)).isCloseTo(95.0d, offset)
		assertThat(kFactor('G', 'QF', (short)5, null)).isCloseTo(90.0d, offset)
		assertThat(kFactor('G', 'R16', (short)5, null)).isCloseTo(85.0d, offset)
		assertThat(kFactor('G', 'R32', (short)5, null)).isCloseTo(80.0d, offset)
		assertThat(kFactor('G', 'R64', (short)5, null)).isCloseTo(75.0d, offset)
		assertThat(kFactor('G', 'R128', (short)5, null)).isCloseTo(70.0d, offset)

		assertThat(kFactor('F', 'F', (short)3, null)).isCloseTo(81.0d, offset)
		assertThat(kFactor('F', 'SF', (short)3, null)).isCloseTo(76.95d, offset)
		assertThat(kFactor('F', 'QF', (short)3, null)).isCloseTo(72.9d, offset)
		assertThat(kFactor('F', 'RR', (short)3, null)).isCloseTo(72.9d, offset)

		assertThat(kFactor('L', 'F', (short)3, null)).isCloseTo(72.0d, offset)
		assertThat(kFactor('L', 'SF', (short)3, null)).isCloseTo(68.4d, offset)
		assertThat(kFactor('L', 'QF', (short)3, null)).isCloseTo(64.8d, offset)
		assertThat(kFactor('L', 'R16', (short)3, null)).isCloseTo(61.2d, offset)

		assertThat(kFactor('M', 'F', (short)5, null)).isCloseTo(80.0d, offset)
		assertThat(kFactor('M', 'F', (short)3, null)).isCloseTo(72.0d, offset)
		assertThat(kFactor('M', 'SF', (short)3, null)).isCloseTo(68.4d, offset)
		assertThat(kFactor('M', 'QF', (short)3, null)).isCloseTo(64.8d, offset)
		assertThat(kFactor('M', 'R16', (short)3, null)).isCloseTo(61.2d, offset)
		assertThat(kFactor('M', 'R32', (short)3, null)).isCloseTo(57.6d, offset)
		assertThat(kFactor('M', 'R64', (short)3, null)).isCloseTo(54.0d, offset)
		assertThat(kFactor('M', 'R128', (short)3, null)).isCloseTo(50.4d, offset)

		assertThat(kFactor('O', 'F', (short)3, null)).isCloseTo(67.5d, offset)
		assertThat(kFactor('O', 'BR', (short)3, null)).isCloseTo(65.8125d, offset)
		assertThat(kFactor('O', 'SF', (short)3, null)).isCloseTo(64.125d, offset)
		assertThat(kFactor('O', 'QF', (short)3, null)).isCloseTo(60.75d, offset)
		assertThat(kFactor('O', 'R16', (short)3, null)).isCloseTo(57.375d, offset)
		assertThat(kFactor('O', 'R32', (short)3, null)).isCloseTo(54.0d, offset)
		assertThat(kFactor('O', 'R64', (short)3, null)).isCloseTo(50.625d, offset)

		assertThat(kFactor('A', 'F', (short)3, null)).isCloseTo(63.0d, offset)
		assertThat(kFactor('A', 'SF', (short)3, null)).isCloseTo(59.85d, offset)
		assertThat(kFactor('A', 'QF', (short)3, null)).isCloseTo(56.7d, offset)
		assertThat(kFactor('A', 'R16', (short)3, null)).isCloseTo(53.55d, offset)
		assertThat(kFactor('A', 'R32', (short)3, null)).isCloseTo(50.4d, offset)
		assertThat(kFactor('A', 'R64', (short)3, null)).isCloseTo(47.25d, offset)

		assertThat(kFactor('B', 'F', (short)3, null)).isCloseTo(54.0d, offset)
		assertThat(kFactor('B', 'SF', (short)3, null)).isCloseTo(51.3d, offset)
		assertThat(kFactor('B', 'QF', (short)3, null)).isCloseTo(48.6d, offset)
		assertThat(kFactor('B', 'R16', (short)3, null)).isCloseTo(45.9d, offset)
		assertThat(kFactor('B', 'R32', (short)3, null)).isCloseTo(43.2d, offset)

		assertThat(kFactor('M', 'R32', (short)3, 'W/O')).isCloseTo(28.8d, offset)
	}

	@Test
	void "kFunction is determined correctly"() {
		def offset = Offset.offset((double)0.000001)

		assertThat(kFunction(1500)).isCloseTo(1.0d, offset)
		assertThat(kFunction(1800)).isCloseTo(1.0d, offset)
		assertThat(kFunction(1850)).isCloseTo(0.875d, offset)
		assertThat(kFunction(1900)).isCloseTo(0.75d, offset)
		assertThat(kFunction(1950)).isCloseTo(0.625d, offset)
		assertThat(kFunction(2000)).isCloseTo(0.5d, offset)
		assertThat(kFunction(2300)).isCloseTo(0.5d, offset)
	}

	@Test
	void "delta rating is calculated as expected"() {
		def offset = Offset.offset((double)0.1)

 		assertThat(deltaRating(2375, 2483, 'G', 'F', (short)5, null)).isCloseTo(65.1d, offset)
 		assertThat(deltaRating(2050, 2506, 'M', 'F', (short)3, null)).isCloseTo(67.1d, offset)
	}
}