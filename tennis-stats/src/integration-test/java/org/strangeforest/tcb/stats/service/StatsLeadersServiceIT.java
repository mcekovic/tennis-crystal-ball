package org.strangeforest.tcb.stats.service;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class StatsLeadersServiceIT {

	@Autowired private StatsLeadersService topPerformersService;

	@Test
	public void statsLeaders() {
		BootgridTable<StatsLeaderRow> statsLeaders = topPerformersService.getStatsLeadersTable("aces", 100, PerfStatsFilter.ALL, 50, "value", 20, 1);

		assertThat(statsLeaders.getRowCount()).isEqualTo(20);
		assertThat(statsLeaders.getRows()).hasSize(20);
	}
}
