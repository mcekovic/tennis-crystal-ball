package org.strangeforest.tcb.dataload

import org.assertj.core.data.*
import org.junit.*

import static org.assertj.core.api.Assertions.*
import static org.strangeforest.tcb.dataload.EloRatings.*

class EloRatingsTest {

	@Test
	void "Start rating is calculated correctly"() {
		assert startRating(   1) == 2394
		assert startRating(   2) == 2323
		assert startRating(   3) == 2275
		assert startRating(   4) == 2237
		assert startRating(   5) == 2204
		assert startRating(   6) == 2182
		assert startRating(   7) == 2160
		assert startRating(   8) == 2145
		assert startRating(   9) == 2131
		assert startRating(  10) == 2117
		assert startRating(  40) == 1942
		assert startRating( 125) == 1765
		assert startRating( 250) == 1648
		assert startRating( 500) == 1598
		assert startRating(1000) == 1500
	}

	static startRating(int rank) {
		new EloRating(100, rank).rating
	}

	@Test
	void "kFactor is determined correctly"() {
		def offset = Offset.offset(0.01d)
		
		assertThat(kFactor('G', 'F', (short)5, null)).isCloseTo(32.00d, offset)
		assertThat(kFactor('G', 'SF', (short)5, null)).isCloseTo(28.80d, offset)
		assertThat(kFactor('G', 'QF', (short)5, null)).isCloseTo(27.20d, offset)
		assertThat(kFactor('G', 'R16', (short)5, null)).isCloseTo(25.60d, offset)
		assertThat(kFactor('G', 'R32', (short)5, null)).isCloseTo(25.60d, offset)
		assertThat(kFactor('G', 'R64', (short)5, null)).isCloseTo(24.00d, offset)
		assertThat(kFactor('G', 'R128', (short)5, null)).isCloseTo(24.00d, offset)

		assertThat(kFactor('F', 'F', (short)3, null)).isCloseTo(25.92d, offset)
		assertThat(kFactor('F', 'SF', (short)3, null)).isCloseTo(23.33d, offset)
		assertThat(kFactor('F', 'QF', (short)3, null)).isCloseTo(22.03d, offset)
		assertThat(kFactor('F', 'RR', (short)3, null)).isCloseTo(22.03d, offset)

		assertThat(kFactor('L', 'F', (short)3, null)).isCloseTo(24.48d, offset)
		assertThat(kFactor('L', 'SF', (short)3, null)).isCloseTo(22.03d, offset)
		assertThat(kFactor('L', 'QF', (short)3, null)).isCloseTo(20.81d, offset)
		assertThat(kFactor('L', 'R16', (short)3, null)).isCloseTo(19.58d, offset)

		assertThat(kFactor('M', 'F', (short)5, null)).isCloseTo(27.20d, offset)
		assertThat(kFactor('M', 'F', (short)3, null)).isCloseTo(24.48d, offset)
		assertThat(kFactor('M', 'SF', (short)3, null)).isCloseTo(22.03d, offset)
		assertThat(kFactor('M', 'QF', (short)3, null)).isCloseTo(20.81d, offset)
		assertThat(kFactor('M', 'R16', (short)3, null)).isCloseTo(19.58d, offset)
		assertThat(kFactor('M', 'R32', (short)3, null)).isCloseTo(19.58d, offset)
		assertThat(kFactor('M', 'R64', (short)3, null)).isCloseTo(18.36d, offset)
		assertThat(kFactor('M', 'R128', (short)3, null)).isCloseTo(18.36d, offset)

		assertThat(kFactor('O', 'F', (short)3, null)).isCloseTo(23.04d, offset)
		assertThat(kFactor('O', 'BR', (short)3, null)).isCloseTo(21.89d, offset)
		assertThat(kFactor('O', 'SF', (short)3, null)).isCloseTo(20.74d, offset)
		assertThat(kFactor('O', 'QF', (short)3, null)).isCloseTo(19.58d, offset)
		assertThat(kFactor('O', 'R16', (short)3, null)).isCloseTo(18.43d, offset)
		assertThat(kFactor('O', 'R32', (short)3, null)).isCloseTo(18.43d, offset)
		assertThat(kFactor('O', 'R64', (short)3, null)).isCloseTo(17.28d, offset)

		assertThat(kFactor('A', 'F', (short)3, null)).isCloseTo(21.60d, offset)
		assertThat(kFactor('A', 'SF', (short)3, null)).isCloseTo(19.44d, offset)
		assertThat(kFactor('A', 'QF', (short)3, null)).isCloseTo(18.36d, offset)
		assertThat(kFactor('A', 'R16', (short)3, null)).isCloseTo(17.28d, offset)
		assertThat(kFactor('A', 'R32', (short)3, null)).isCloseTo(17.28d, offset)
		assertThat(kFactor('A', 'R64', (short)3, null)).isCloseTo(16.20d, offset)

		assertThat(kFactor('B', 'F', (short)3, null)).isCloseTo(20.16d, offset)
		assertThat(kFactor('B', 'SF', (short)3, null)).isCloseTo(18.14d, offset)
		assertThat(kFactor('B', 'QF', (short)3, null)).isCloseTo(17.14d, offset)
		assertThat(kFactor('B', 'R16', (short)3, null)).isCloseTo(16.13d, offset)
		assertThat(kFactor('B', 'R32', (short)3, null)).isCloseTo(16.13d, offset)

		assertThat(kFactor('M', 'R32', (short)3, 'W/O')).isCloseTo(9.79d, offset)
	}

	@Test
	void "kFunction is determined correctly"() {
		def offset = Offset.offset(0.001d)

		assertThat(kFunction(1500d)).isCloseTo(10.000d, offset)
		assertThat(kFunction(1600d)).isCloseTo(5.495d, offset)
		assertThat(kFunction(1800d)).isCloseTo(1.640d, offset)
		assertThat(kFunction(2000d)).isCloseTo(1.073d, offset)
		assertThat(kFunction(2200d)).isCloseTo(1.008d, offset)
		assertThat(kFunction(2500d)).isCloseTo(1.000d, offset)
	}

	@Test
	void "delta rating is calculated as expected"() {
		def offset = Offset.offset(0.1d)

		def delta1 = deltaRating(2450, 2350, 'G', 'F', (short)5, null)
		assertThat(delta1).isCloseTo(11.5d, offset)
		assertThat(kFunction(2450) * delta1).isCloseTo(11.5d, offset)
		assertThat(kFunction(2350) * -delta1).isCloseTo(-11.5d, offset)

		def delta2 = deltaRating(2000, 2350, 'M', 'SF', (short)3, null)
		assertThat(delta2).isCloseTo(19.4d, offset)
		assertThat(kFunction(2000) * delta2).isCloseTo(20.9d, offset)
		assertThat(kFunction(2350) * -delta2).isCloseTo(-19.4d, offset)

		def delta3 = deltaRating(2250, 1800, 'B', 'R32', (short)3, null)
		assertThat(delta3).isCloseTo(1.1d, offset)
		assertThat(kFunction(2250) * delta3).isCloseTo(1.1d, offset)
		assertThat(kFunction(1800) * -delta3).isCloseTo(-1.8d, offset)
	}
}