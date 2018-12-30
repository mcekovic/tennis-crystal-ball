package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.forecast.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

class NoTBSetOutcomeTest {

	@Test
	void testEqualP() {
		SetOutcome set = new SetOutcome(0.5, 0.5, NO_TIE_BREAK);

		assertThat(set.pWin()).isEqualTo(0.5);
	}

	@Test
	void testFinalStep() {
		SetOutcome set = new SetOutcome(0.8, 0.4, NO_TIE_BREAK);

		assertThat(set.pWin(6, 0)).isEqualTo(1.0);
		assertThat(set.pWin(6, 1)).isEqualTo(1.0);
		assertThat(set.pWin(6, 2)).isEqualTo(1.0);
		assertThat(set.pWin(6, 3)).isEqualTo(1.0);
		assertThat(set.pWin(6, 4)).isEqualTo(1.0);
		assertThat(set.pWin(0, 6)).isEqualTo(0.0);
		assertThat(set.pWin(1, 6)).isEqualTo(0.0);
		assertThat(set.pWin(2, 6)).isEqualTo(0.0);
		assertThat(set.pWin(3, 6)).isEqualTo(0.0);
		assertThat(set.pWin(4, 6)).isEqualTo(0.0);
	}

	@Test
	void testDeuce() {
		SetOutcome set = new SetOutcome(0.75, 0.25, NO_TIE_BREAK);

		assertThat(set.pWin(6, 5)).isEqualTo(0.974609375);
		assertThat(set.pWin(6, 6)).isEqualTo(0.5);
		assertThat(set.pWin(5, 6)).isEqualTo(0.474609375);
	}
}
