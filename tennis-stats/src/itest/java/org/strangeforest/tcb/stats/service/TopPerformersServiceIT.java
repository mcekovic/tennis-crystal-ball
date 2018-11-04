package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;

@ServiceTest
class TopPerformersServiceIT {

	@Autowired private TopPerformersService topPerformersService;

	@Test
	void goatTopN() {
		BootgridTable<TopPerformerRow> topPerformers = topPerformersService.getTopPerformersTable("matches", 100, PerfStatsFilter.ALL, 50, "won_lost_pct", 20, 1);

		assertThat(topPerformers.getRowCount()).isEqualTo(20);
		assertThat(topPerformers.getRows()).hasSize(20);
	}
}
