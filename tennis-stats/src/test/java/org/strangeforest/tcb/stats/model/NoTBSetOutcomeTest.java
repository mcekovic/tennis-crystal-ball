package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.SetRules.*;

class NoTBSetOutcomeTest {

	private static final Offset<Double> OFFSET = Offset.offset(1E-10);

	@Test
	void testEqualP() {
		SetOutcome set = new SetOutcome(0.6, 0.4, NO_TB_SET);

		assertThat(set.pWin()).isEqualTo(0.5, OFFSET);
	}

	@Test
	void testFinalStep() {
		SetOutcome set = new SetOutcome(0.8, 0.4, NO_TB_SET);

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
		SetOutcome set = new SetOutcome(0.75, 0.35, NO_TB_SET);

		assertThat(set.pWin(6, 5)).isEqualTo(0.9895040835, OFFSET);
		assertThat(set.pWin(6, 6)).isEqualTo(0.7933111835, OFFSET);
		assertThat(set.pWin(5, 6)).isEqualTo(0.7530258500, OFFSET);
	}

	@Test
	void testTBpDiff() {
		double pTB = new SetOutcome(0.75, 0.35, COMMON_SET).pWin();
		double pNoTB = new SetOutcome(0.75, 0.35, NO_TB_SET).pWin();

		assertThat(pTB).isEqualTo(0.7817143146, OFFSET);
		assertThat(pNoTB).isEqualTo(0.8196805996, OFFSET);
	}
}
