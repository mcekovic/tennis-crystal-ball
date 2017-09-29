package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class RefreshComputedDataJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshComputedDataJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.refresh-computed-data:0 25 1 * * MON}")
	public void refreshComputedData() {
		String storageOption = dataService.getDBServerVersion() >= DataService.MATERIALIZED_VIEWS_MIN_VERSION ? "-m" : "-t";
		if (dataLoadCommand.execute("RefreshComputedData", "-rc", "-c 1", storageOption) == 0) {
			try {
				dataLoadCommand.execute("Vacuum", "-vc", "-c 1", storageOption);
			}
			finally {
				clearCaches();
			}
		}
	}

	private void clearCaches() {
		int cacheCount = dataService.clearCaches(".*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
