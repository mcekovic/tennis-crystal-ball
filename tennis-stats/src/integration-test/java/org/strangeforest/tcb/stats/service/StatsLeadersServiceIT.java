package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ServiceTest
class StatsLeadersServiceIT {

	@Autowired private StatsLeadersService topPerformersService;

	@Test
	void statsLeaders() {
		BootgridTable<StatsLeaderRow> statsLeaders = topPerformersService.getStatsLeadersTable("aces", 100, PerfStatsFilter.ALL, 50, "value", 20, 1);

		assertThat(statsLeaders.getRowCount()).isEqualTo(20);
		assertThat(statsLeaders.getRows()).hasSize(20);
	}
}
