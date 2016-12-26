package org.strangeforest.tcb.model;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;

public class TieBreakOutcomeTest {

	@Test
	public void testEqualP() {
		TieBreakOutcome tieBreak = new TieBreakOutcome(0.5, 0.5);

		assertThat(tieBreak.pWin()).isEqualTo(0.5);
	}

	@Test
	public void testFinalStep() {
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
	public void testDeuce() {
		TieBreakOutcome tieBreak = new TieBreakOutcome(0.75, 0.25);

		assertThat(tieBreak.pWin(7, 6)).isEqualTo(0.625);
		assertThat(tieBreak.pWin(7, 7)).isEqualTo(0.5);
		assertThat(tieBreak.pWin(6, 7)).isEqualTo(0.125);
	}
}
