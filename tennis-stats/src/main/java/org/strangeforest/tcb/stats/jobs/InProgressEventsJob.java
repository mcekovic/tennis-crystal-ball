package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class InProgressEventsJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(InProgressEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.reload-in-progress-events:0 0/15 * * * *}")
	public void reloadInProgressEvents() {
		reloadInProgressEvents(new String[] {"-ip", "-c 1"});
	}

	@Scheduled(cron = "${tennis-stats.jobs.reload-in-progress-events-forced:0 5 6 * * MON}")
	public void reloadInProgressEventsForced() {
		reloadInProgressEvents(new String[] {"-ip", "-c 1", "-ff"});
	}

	private void reloadInProgressEvents(String[] params) {
		if (dataLoadCommand.execute("ReloadInProgressEvents", params) == 0)
			clearCaches();
	}

	private void clearCaches() {
		dataService.evictGlobal("InProgressEvents");
		int cacheCount = dataService.clearCaches("InProgressEvent.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
