package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;

class TieBreakOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		TieBreakOutcome tieBreak = new TieBreakOutcome(0.8, 0.2);

		assertThat(tieBreak.pWin()).isCloseTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		TieBreakOutcome tieBreak = new TieBreakOutcome(0.8, 0.4);

		assertThat(tieBreak.pWin(7, 0)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(7, 1)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(7, 2)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(7, 3)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(7, 4)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(7, 5)).isEqualTo(1.0);
		assertThat(tieBreak.pWin(0, 7)).isEqualTo(0.0);
		assertThat(tieBreak.pWin(1, 7)).isEqualTo(0.0);
		assertThat(tieBreak.pWin(2, 7)).isEqualTo(0.0);
		assertThat(tieBreak.pWin(3, 7)).isEqualTo(0.0);
		assertThat(tieBreak.pWin(4, 7)).isEqualTo(0.0);
		assertThat(tieBreak.pWin(5, 7)).isEqualTo(0.0);
	}

	@Test
	void testDeuce() {
		TieBreakOutcome tieBreak = new TieBreakOutcome(0.75, 0.25);

		assertThat(tieBreak.pWin(7, 6)).isEqualTo(0.625);
		assertThat(tieBreak.pWin(7, 7)).isEqualTo(0.5);
		assertThat(tieBreak.pWin(6, 7)).isEqualTo(0.125);
	}

	@Test
	void testSymmetry() {
		TieBreakOutcome tieBreakOutcome = new TieBreakOutcome(0.75, 0.25);

		assertThat(tieBreakOutcome.pWin(0, 0)).isEqualTo(tieBreakOutcome.pWin(0, 0, true), OFFSET);
		assertThat(tieBreakOutcome.pWin(0, 0, true)).isEqualTo(1.0 - tieBreakOutcome.invertedOutcome().pWin(0, 0, false), OFFSET);

		assertThat(tieBreakOutcome.pWin(3, 3)).isEqualTo(tieBreakOutcome.pWin(3, 3, true), OFFSET);
		assertThat(tieBreakOutcome.pWin(3, 3, true)).isEqualTo(1.0 - tieBreakOutcome.invertedOutcome().pWin(3, 3, false), OFFSET);

		assertThat(tieBreakOutcome.pWin(4, 2)).isEqualTo(tieBreakOutcome.pWin(4, 2, true), OFFSET);
		assertThat(tieBreakOutcome.pWin(4, 2, true)).isEqualTo(1.0 - tieBreakOutcome.invertedOutcome().pWin(2, 4, false), OFFSET);
	}
}
