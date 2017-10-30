package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class NewRankingsJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(NewRankingsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.load-new-rankings:0 47 5 * * MON}")
	public void loadNewRankings() {
		if (dataLoadCommand.execute("NewRankings", "-nr", "-c 1") == 0)
			clearCaches();
	}

	private void clearCaches() {
		int cacheCount = dataService.clearCaches("Rankings.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
