package org.strangeforest.tcb.model

import org.junit.jupiter.api.*

import org.assertj.core.api.Assertions.*

class BestOf5MatchOutcomeTest {

	@Test
	fun testEqualP() {
		val match = MatchOutcome(0.5, 0.5, 5)

		assertThat(match.pWin()).isEqualTo(0.5)
	}

	@Test
	fun testFinalStep() {
		val match = MatchOutcome(0.8, 0.4, 5)

		assertThat(match.pWin(3, 0)).isEqualTo(1.0)
		assertThat(match.pWin(3, 1)).isEqualTo(1.0)
		assertThat(match.pWin(3, 2)).isEqualTo(1.0)
		assertThat(match.pWin(0, 3)).isEqualTo(0.0)
		assertThat(match.pWin(1, 3)).isEqualTo(0.0)
		assertThat(match.pWin(2, 3)).isEqualTo(0.0)
	}
}