package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.MatchRules.*;

class MatchRulesTest {

	@Test
	void setIsNotDeciding() {
		assertThat(BEST_OF_3_MATCH.isDecidingSet(0)).isFalse();
		assertThat(BEST_OF_3_MATCH.isDecidingSet(1)).isFalse();

		assertThat(BEST_OF_5_MATCH.isDecidingSet(0)).isFalse();
		assertThat(BEST_OF_5_MATCH.isDecidingSet(1)).isFalse();
		assertThat(BEST_OF_5_MATCH.isDecidingSet(2)).isFalse();
		assertThat(BEST_OF_5_MATCH.isDecidingSet(3)).isFalse();
	}

	@Test
	void setIsDeciding() {
		assertThat(BEST_OF_3_MATCH.isDecidingSet(2)).isTrue();

		assertThat(BEST_OF_5_MATCH.isDecidingSet(4)).isTrue();
	}
}
