package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.MatchRules.*;

class BestOf5MatchOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		var match = new MatchOutcome(0.65, 0.35, BEST_OF_5_MATCH);

		assertThat(match.pWin()).isEqualTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		var match = new MatchOutcome(0.8, 0.4, BEST_OF_5_MATCH);

		assertThat(match.pWin(3, 0)).isEqualTo(1.0);
		assertThat(match.pWin(3, 1)).isEqualTo(1.0);
		assertThat(match.pWin(3, 2)).isEqualTo(1.0);
		assertThat(match.pWin(0, 3)).isEqualTo(0.0);
		assertThat(match.pWin(1, 3)).isEqualTo(0.0);
		assertThat(match.pWin(2, 3)).isEqualTo(0.0);
	}

	@Test
	void testSymmetry() {
		var match = new MatchOutcome(0.8, 0.4, BEST_OF_5_MATCH);

		assertThat(match.pWin(0, 0)).isEqualTo(match.pWin(0, 0, 0, 0, 0, 0, true).getPMatch(), OFFSET);
		assertThat(match.pWin(0, 0, 0, 0, 0, 0, true).getPMatch()).isEqualTo(match.pWin(0, 0, 0, 0, 0, 0, false).getPMatch(), OFFSET);
	}
}