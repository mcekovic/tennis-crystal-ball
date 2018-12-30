package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.forecast.*;

import static org.assertj.core.api.Assertions.*;

class BestOf3MatchOutcomeTest {

	@Test
	void testEqualP() {
		MatchOutcome match = new MatchOutcome(0.5, 0.5, 2);

		assertThat(match.pWin()).isEqualTo(0.5);
	}

	@Test
	void testFinalStep() {
		MatchOutcome match = new MatchOutcome(0.8, 0.4, 2);

		assertThat(match.pWin(2, 0)).isEqualTo(1.0);
		assertThat(match.pWin(2, 1)).isEqualTo(1.0);
		assertThat(match.pWin(0, 2)).isEqualTo(0.0);
		assertThat(match.pWin(1, 2)).isEqualTo(0.0);
	}
}
