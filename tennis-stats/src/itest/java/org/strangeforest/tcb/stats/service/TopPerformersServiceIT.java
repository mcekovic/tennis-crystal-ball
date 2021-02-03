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
	void topPerformers() {
		var topPerformers = topPerformersService.getTopPerformersTable("matches", 100, PerfStatsFilter.ALL, 50, "won_lost_pct DESC", 20, 1);

		assertThat(topPerformers.getRowCount()).isEqualTo(20);
		assertThat(topPerformers.getRows()).hasSize(20);
	}

	@Test
	void titlesAndResults() {
		var topPerformers = topPerformersService.getTitlesAndResultsTable(100, PerfStatsFilter.ALL, "count DESC", 20, 1);

		assertThat(topPerformers.getRowCount()).isEqualTo(20);
		assertThat(topPerformers.getRows()).hasSize(20);
	}

	@Test
	void mentalToughness() {
		var mentalToughness = topPerformersService.getMentalToughnessTable(100, PerfStatsFilter.ALL, 50, "rating DESC", 20, 1);

		assertThat(mentalToughness.getRowCount()).isEqualTo(20);
		assertThat(mentalToughness.getRows()).hasSize(20);
	}
}
