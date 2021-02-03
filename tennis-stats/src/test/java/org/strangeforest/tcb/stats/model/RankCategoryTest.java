package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;

class RankCategoryTest {

	@Test
	void rankCategoryIsLoaded() {
		assertThat(RankCategory.values()).isNotEmpty();
		for (var category : RankCategory.values())
			assertThat(category.getRankTypes()).isNotEmpty();
	}
}
