package org.strangeforest.tcb.stats.service;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class DominanceTimelineServiceIT {

	@Autowired private DominanceTimelineService dominanceTimelineService;

	@Test
	public void dominanceTimeline() {
		DominanceTimeline dominanceTimeline = dominanceTimelineService.getDominanceTimeline(null);

		assertThat(dominanceTimeline.getSeasons().size()).isPositive();
		assertThat(dominanceTimeline.getPlayers().size()).isPositive();
		assertThat(dominanceTimeline.getDominanceEras().size()).isPositive();
	}
}
