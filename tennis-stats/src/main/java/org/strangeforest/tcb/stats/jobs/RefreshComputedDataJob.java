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
public class RefreshComputedDataJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshComputedDataJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.refresh-computed-data:0 25 2 * * MON}")
	public void refreshComputedData() {
		String storageOption = dataService.getDBServerVersion() >= DataService.MATERIALIZED_VIEWS_MIN_VERSION ? "-m" : "-t";
		if (dataLoad("RefreshComputedData", "-rc", "-c 1", storageOption) == 0) {
			try {
				dataLoad("Vacuum", "-vc", "-c 1", storageOption);
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
