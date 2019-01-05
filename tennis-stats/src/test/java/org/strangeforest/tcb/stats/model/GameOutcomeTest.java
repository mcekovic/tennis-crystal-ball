package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;

class GameOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		GameOutcome game = new GameOutcome(0.5);

		assertThat(game.pWin()).isEqualTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		GameOutcome game = new GameOutcome(0.8);

		assertThat(game.pWin(4, 0)).isEqualTo(1.0);
		assertThat(game.pWin(4, 1)).isEqualTo(1.0);
		assertThat(game.pWin(4, 2)).isEqualTo(1.0);
		assertThat(game.pWin(0, 4)).isEqualTo(0.0);
		assertThat(game.pWin(1, 4)).isEqualTo(0.0);
		assertThat(game.pWin(2, 4)).isEqualTo(0.0);
	}

	@Test
	void testDeuce() {
		GameOutcome game = new GameOutcome(0.75);

		assertThat(game.pWin(4, 3)).isEqualTo(0.975, OFFSET);
		assertThat(game.pWin(4, 4)).isEqualTo(0.9, OFFSET);
		assertThat(game.pWin(3, 4)).isEqualTo(0.675, OFFSET);
	}
}
