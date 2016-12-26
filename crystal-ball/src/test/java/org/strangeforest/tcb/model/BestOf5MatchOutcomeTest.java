package org.strangeforest.tcb.model;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;

public class BestOf5MatchOutcomeTest {

	@Test
	public void testEqualP() {
		MatchOutcome match = new MatchOutcome(0.5, 0.5, 5);

		assertThat(match.pWin()).isEqualTo(0.5);
	}

	@Test
	public void testFinalStep() {
		MatchOutcome match = new MatchOutcome(0.8, 0.4, 5);

		assertThat(match.pWin(3, 0)).isEqualTo(1.0);
		assertThat(match.pWin(3, 1)).isEqualTo(1.0);
		assertThat(match.pWin(3, 2)).isEqualTo(1.0);
		assertThat(match.pWin(0, 3)).isEqualTo(0.0);
		assertThat(match.pWin(1, 3)).isEqualTo(0.0);
		assertThat(match.pWin(2, 3)).isEqualTo(0.0);
	}
}