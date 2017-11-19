package org.strangeforest.tcb.stats.model;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;

public class PlayerRowTest {

	@Test
	public void nameIsShort() {
		PlayerRow player = new PlayerRow(10, 1000, "Juan Martin Del Potro", "ARG", true);

		assertThat(player.shortName()).isEqualTo("J. M. D. Potro");
	}
}
