package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;

import static org.assertj.core.api.Assertions.*;

@ServiceTest
class DominanceTimelineServiceIT {

	@Autowired private DominanceTimelineService dominanceTimelineService;

	@Test
	void dominanceTimeline() {
		DominanceTimeline dominanceTimeline = dominanceTimelineService.getDominanceTimeline(null);

		assertThat(dominanceTimeline.getSeasons().size()).isPositive();
		assertThat(dominanceTimeline.getPlayers().size()).isPositive();
		assertThat(dominanceTimeline.getDominanceEras().size()).isPositive();
	}
}
