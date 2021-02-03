package org.strangeforest.tcb.stats.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class PlayerRowTest {

	@Test
	void nameIsShort() {
		var player = new PlayerRow(10, 1000, "Juan Martin Del Potro", "ARG", true);

		assertThat(player.shortName()).isEqualTo("J. M. D. Potro");
	}
}
