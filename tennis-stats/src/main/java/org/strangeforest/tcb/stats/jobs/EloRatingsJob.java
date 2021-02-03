package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class EloRatingsJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(EloRatingsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.compute-elo-ratings:0 46 5 * * MON}")
	public void computeEloRatings() {
		if (dataLoadCommand.execute("EloRatings", "-el", "-d") == 0)
			clearCaches();
	}

	private void clearCaches() {
		var cacheCount = dataService.clearCaches("Rankings.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
