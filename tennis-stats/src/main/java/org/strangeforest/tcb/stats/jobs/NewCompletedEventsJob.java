package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class NewCompletedEventsJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(NewCompletedEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.load-new-completed-events:0 40 5 * * *}")
	public void loadNewCompletedEvents() {
		if (dataLoadCommand.execute("LoadNewCompletedEvents", "-nt", "-c 1") == 0)
			clearCaches();
	}

	private void clearCaches() {
		dataService.evictGlobal("Tournaments");
		int cacheCount = dataService.clearCaches("Tournament.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
