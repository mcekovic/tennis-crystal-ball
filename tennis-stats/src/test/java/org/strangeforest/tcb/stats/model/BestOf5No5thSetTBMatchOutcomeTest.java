package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.MatchRules.*;

class BestOf5No5thSetTBMatchOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		MatchOutcome match = new MatchOutcome(0.7, 0.3, BEST_OF_5_NO_5TH_SET_TB_MATCH);

		assertThat(match.pWin()).isEqualTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		MatchOutcome match = new MatchOutcome(0.8, 0.4, BEST_OF_5_NO_5TH_SET_TB_MATCH);

		assertThat(match.pWin(3, 0)).isEqualTo(1.0);
		assertThat(match.pWin(3, 1)).isEqualTo(1.0);
		assertThat(match.pWin(3, 2)).isEqualTo(1.0);
		assertThat(match.pWin(0, 3)).isEqualTo(0.0);
		assertThat(match.pWin(1, 3)).isEqualTo(0.0);
		assertThat(match.pWin(2, 3)).isEqualTo(0.0);
	}

	@Test
	void testTBpDiff() {
		MatchOutcome bestOf5 = new MatchOutcome(0.7, 0.35, BEST_OF_5_MATCH);
		MatchOutcome bestOf5NoTB = new MatchOutcome(0.7, 0.35, BEST_OF_5_NO_5TH_SET_TB_MATCH);

		assertThat(bestOf5.pWin()).isEqualTo(0.7734624402, OFFSET);
		assertThat(bestOf5NoTB.pWin()).isEqualTo(0.7792698753, OFFSET);
	}

	@Test
	void testSymmetry() {
		MatchOutcome match = new MatchOutcome(0.8, 0.4, BEST_OF_5_NO_5TH_SET_TB_MATCH);

		assertThat(match.pWin(0, 0)).isEqualTo(match.pWin(0, 0, 0, 0, 0, 0, true).getPMatch(), OFFSET);
		assertThat(match.pWin(0, 0, 0, 0, 0, 0, true).getPMatch()).isEqualTo(match.pWin(0, 0, 0, 0, 0, 0, false).getPMatch(), OFFSET);
	}
}