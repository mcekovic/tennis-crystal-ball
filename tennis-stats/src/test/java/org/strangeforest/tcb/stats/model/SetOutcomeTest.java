package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;

class SetOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		SetOutcome set = new SetOutcome(0.75, 0.25);

		assertThat(set.pWin()).isEqualTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		SetOutcome set = new SetOutcome(0.8, 0.4);

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
		SetOutcome set = new SetOutcome(0.75, 0.35);

		assertThat(set.pWin(6, 5)).isEqualTo(0.9832912945, OFFSET);
		assertThat(set.pWin(6, 6)).isEqualTo(0.6709670307, OFFSET);
		assertThat(set.pWin(5, 6)).isEqualTo(0.6368944861, OFFSET);

		assertThat(set.pWin(11, 10)).isEqualTo(0.9832912945, OFFSET);
		assertThat(set.pWin(10, 10)).isEqualTo(0.6709670307, OFFSET);
		assertThat(set.pWin(10, 11)).isEqualTo(0.6368944861, OFFSET);
		assertThat(set.pWin(50, 10)).isEqualTo(1.0, OFFSET);
		assertThat(set.pWin(10, 50)).isEqualTo(0.0, OFFSET);
	}

	@Test
	void testSymmetry() {
		SetOutcome set = new SetOutcome(0.75, 0.35);

		assertThat(set.pWin(0, 0)).isEqualTo(set.pWin(0, 0, true), OFFSET);
		assertThat(set.pWin(0, 0, true)).isEqualTo(1.0 - set.invertedOutcome().pWin(0, 0, false), OFFSET);

		assertThat(set.pWin(3, 3)).isEqualTo(set.pWin(3, 3, true), OFFSET);
		assertThat(set.pWin(3, 3, true)).isEqualTo(1.0 - set.invertedOutcome().pWin(3, 3, false), OFFSET);

		assertThat(set.pWin(4, 2)).isEqualTo(set.pWin(4, 2, true), OFFSET);
		assertThat(set.pWin(4, 2, true)).isEqualTo(1.0 - set.invertedOutcome().pWin(2, 4, false), OFFSET);
	}
}
