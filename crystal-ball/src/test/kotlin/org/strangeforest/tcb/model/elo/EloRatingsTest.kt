package org.strangeforest.tcb.model.elo

import org.assertj.core.api.Assertions.*
import org.assertj.core.data.*
import org.junit.jupiter.api.*
import org.strangeforest.tcb.model.elo.EloRatings.Companion.kFactor

class EloRatingsTest {

	@Test
	internal fun kFactorIsDeterminedCorrectly() {
		val offset = Offset.offset(0.01)

		assertThat(kFactor("G", "F", 5, null)).isCloseTo(32.00, offset)
		assertThat(kFactor("G", "SF", 5, null)).isCloseTo(28.80, offset)
		assertThat(kFactor("G", "QF", 5, null)).isCloseTo(27.20, offset)
		assertThat(kFactor("G", "R16", 5, null)).isCloseTo(25.60, offset)
		assertThat(kFactor("G", "R32", 5, null)).isCloseTo(25.60, offset)
		assertThat(kFactor("G", "R64", 5, null)).isCloseTo(24.00, offset)
		assertThat(kFactor("G", "R128", 5, null)).isCloseTo(24.00, offset)

		assertThat(kFactor("F", "F", 3, null)).isCloseTo(25.92, offset)
		assertThat(kFactor("F", "SF", 3, null)).isCloseTo(23.33, offset)
		assertThat(kFactor("F", "QF", 3, null)).isCloseTo(22.03, offset)
		assertThat(kFactor("F", "RR", 3, null)).isCloseTo(22.03, offset)

		assertThat(kFactor("L", "F", 3, null)).isCloseTo(24.48, offset)
		assertThat(kFactor("L", "SF", 3, null)).isCloseTo(22.03, offset)
		assertThat(kFactor("L", "QF", 3, null)).isCloseTo(20.81, offset)
		assertThat(kFactor("L", "R16", 3, null)).isCloseTo(19.58, offset)

		assertThat(kFactor("M", "F", 5, null)).isCloseTo(27.20, offset)
		assertThat(kFactor("M", "F", 3, null)).isCloseTo(24.48, offset)
		assertThat(kFactor("M", "SF", 3, null)).isCloseTo(22.03, offset)
		assertThat(kFactor("M", "QF", 3, null)).isCloseTo(20.81, offset)
		assertThat(kFactor("M", "R16", 3, null)).isCloseTo(19.58, offset)
		assertThat(kFactor("M", "R32", 3, null)).isCloseTo(19.58, offset)
		assertThat(kFactor("M", "R64", 3, null)).isCloseTo(18.36, offset)
		assertThat(kFactor("M", "R128", 3, null)).isCloseTo(18.36, offset)

		assertThat(kFactor("O", "F", 3, null)).isCloseTo(23.04, offset)
		assertThat(kFactor("O", "BR", 3, null)).isCloseTo(21.89, offset)
		assertThat(kFactor("O", "SF", 3, null)).isCloseTo(20.74, offset)
		assertThat(kFactor("O", "QF", 3, null)).isCloseTo(19.58, offset)
		assertThat(kFactor("O", "R16", 3, null)).isCloseTo(18.43, offset)
		assertThat(kFactor("O", "R32", 3, null)).isCloseTo(18.43, offset)
		assertThat(kFactor("O", "R64", 3, null)).isCloseTo(17.28, offset)

		assertThat(kFactor("A", "F", 3, null)).isCloseTo(21.60, offset)
		assertThat(kFactor("A", "SF", 3, null)).isCloseTo(19.44, offset)
		assertThat(kFactor("A", "QF", 3, null)).isCloseTo(18.36, offset)
		assertThat(kFactor("A", "R16", 3, null)).isCloseTo(17.28, offset)
		assertThat(kFactor("A", "R32", 3, null)).isCloseTo(17.28, offset)
		assertThat(kFactor("A", "R64", 3, null)).isCloseTo(16.20, offset)

		assertThat(kFactor("B", "F", 3, null)).isCloseTo(20.16, offset)
		assertThat(kFactor("B", "SF", 3, null)).isCloseTo(18.14, offset)
		assertThat(kFactor("B", "QF", 3, null)).isCloseTo(17.14, offset)
		assertThat(kFactor("B", "R16", 3, null)).isCloseTo(16.13, offset)
		assertThat(kFactor("B", "R32", 3, null)).isCloseTo(16.13, offset)

		assertThat(kFactor("M", "R32", 3, "W/O")).isCloseTo(9.79, offset)
	}
}
