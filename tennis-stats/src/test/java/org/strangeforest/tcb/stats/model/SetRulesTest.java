package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.SetRules.*;

class SetRulesTest {

	@Test
	void isTieBreak() {
		assertThat(COMMON_SET.isTieBreak(6, 6)).isTrue();
		assertThat(AO_5TH_SET.isTieBreak(6, 6)).isTrue();
		assertThat(WB_5TH_SET.isTieBreak(12, 12)).isTrue();
	}

	@Test
	void setNotTieBreak() {
		assertThat(COMMON_SET.isTieBreak(6, 5)).isFalse();
		assertThat(COMMON_SET.isTieBreak(6, 4)).isFalse();

		assertThat(NO_TB_SET.isTieBreak(6, 6)).isFalse();

		assertThat(WB_5TH_SET.isTieBreak(6, 6)).isFalse();
	}
}
