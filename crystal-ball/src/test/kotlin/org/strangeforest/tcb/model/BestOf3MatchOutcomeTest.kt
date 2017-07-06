package org.strangeforest.tcb.model

import org.junit.*

import org.assertj.core.api.Assertions.*

class BestOf3MatchOutcomeTest {

	@Test
	fun testEqualP() {
		val match = MatchOutcome(0.5, 0.5, 3)

		assertThat(match.pWin()).isEqualTo(0.5)
	}

	@Test
	fun testFinalStep() {
		val match = MatchOutcome(0.8, 0.4, 3)

		assertThat(match.pWin(2, 0)).isEqualTo(1.0)
		assertThat(match.pWin(2, 1)).isEqualTo(1.0)
		assertThat(match.pWin(0, 2)).isEqualTo(0.0)
		assertThat(match.pWin(1, 2)).isEqualTo(0.0)
	}
}
