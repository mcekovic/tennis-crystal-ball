package org.strangeforest.tcb.model

import org.junit.*

import org.assertj.core.api.Assertions.*

class GameOutcomeTest {

	@Test
	fun testEqualP() {
		val game = GameOutcome(0.5)

		assertThat(game.pWin()).isEqualTo(0.5)
	}

	@Test
	fun testFinalStep() {
		val game = GameOutcome(0.8)

		assertThat(game.pWin(4, 0)).isEqualTo(1.0)
		assertThat(game.pWin(4, 1)).isEqualTo(1.0)
		assertThat(game.pWin(4, 2)).isEqualTo(1.0)
		assertThat(game.pWin(0, 4)).isEqualTo(0.0)
		assertThat(game.pWin(1, 4)).isEqualTo(0.0)
		assertThat(game.pWin(2, 4)).isEqualTo(0.0)
	}

	@Test
	fun testDeuce() {
		val game = GameOutcome(0.75)

		assertThat(game.pWin(4, 3)).isEqualTo(0.975)
		assertThat(game.pWin(4, 4)).isEqualTo(0.9)
		assertThat(game.pWin(3, 4)).isEqualTo(0.675)
	}
}
