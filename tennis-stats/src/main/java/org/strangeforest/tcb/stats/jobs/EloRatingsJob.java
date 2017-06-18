package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.jobs.DataLoadCommand.*;

@Component
@Profile("openshift")
public class EloRatingsJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(EloRatingsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.compute-elo-ratings:0 20 2 * * MON}")
	public void computeEloRatings() {
		if (dataLoad("EloRatings", "-el", "-c 3", "-d") == 0)
			clearCaches();
	}

	private void clearCaches() {
		int cacheCount = dataService.clearCaches("Rankings.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
