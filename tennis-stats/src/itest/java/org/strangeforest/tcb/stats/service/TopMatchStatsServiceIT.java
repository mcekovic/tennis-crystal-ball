package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;

@ServiceTest
class TopMatchStatsServiceIT {

	@Autowired private TopMatchStatsService topMatchStatsService;

	@Test
	void topMatchStats() {
		int playerCount = topMatchStatsService.getPlayerCount("aces", PerfStatsFilter.ALL);
		BootgridTable<TopMatchStatsRow> topMatchStats = topMatchStatsService.getTopMatchStatsTable("aces", playerCount, PerfStatsFilter.ALL, "value DESC", 20, 1);

		assertThat(topMatchStats.getRowCount()).isEqualTo(20);
		assertThat(topMatchStats.getRows()).hasSize(20);
	}
}
