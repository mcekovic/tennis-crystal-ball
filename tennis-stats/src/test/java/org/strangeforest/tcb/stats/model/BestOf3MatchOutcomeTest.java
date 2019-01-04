package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.MatchRules.*;

class BestOf3MatchOutcomeTest {

	@Test
	void testEqualP() {
		MatchOutcome match = new MatchOutcome(0.6, 0.4, BEST_OF_3_MATCH);

		assertThat(match.pWin()).isCloseTo(0.5, Offset.offset(1E-10));
	}

	@Test
	void testFinalStep() {
		MatchOutcome match = new MatchOutcome(0.8, 0.4, BEST_OF_3_MATCH);

		assertThat(match.pWin(2, 0)).isEqualTo(1.0);
		assertThat(match.pWin(2, 1)).isEqualTo(1.0);
		assertThat(match.pWin(0, 2)).isEqualTo(0.0);
		assertThat(match.pWin(1, 2)).isEqualTo(0.0);
	}
}
