package org.strangeforest.tcb.stats.model;

import org.junit.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;

public class RankTypeTest {

	@Test
	public void rankTypeIsLoaded() {
		assertThat(RankType.values()).isNotEmpty();
	}
}
