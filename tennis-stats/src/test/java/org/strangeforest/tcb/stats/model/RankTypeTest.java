package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.RankType.*;

class RankTypeTest {

	@Test
	void rankTypeIsLoaded() {
		assertThat(values()).isNotEmpty();
	}

	@Test
	void rankTypesAndPointTypeAreLinked() {
		assertThat(RANK.rankType).isEqualTo(RANK);
		assertThat(RANK.pointsType).isEqualTo(POINTS);
		assertThat(POINTS.rankType).isEqualTo(RANK);
		assertThat(POINTS.pointsType).isEqualTo(POINTS);
	}
}
