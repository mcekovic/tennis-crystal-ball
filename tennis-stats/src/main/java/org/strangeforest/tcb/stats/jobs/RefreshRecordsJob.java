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
public class RefreshRecordsJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshRecordsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.refresh-records:0 5 2 * * MON}")
	public void refreshRecords() {
		String storageOption = dataService.getDBServerVersion() >= DataService.MATERIALIZED_VIEWS_MIN_VERSION ? "-m" : "-t";
		if (dataLoad("RefreshRecords", "-rr", "-c 1", "-rp 500", storageOption) == 0) {
			try {
				dataLoad("Vacuum", "-vc", "-c 1", storageOption);
			}
			finally {
				clearCaches();
			}
		}
	}

	private void clearCaches() {
		int cacheCount = dataService.clearCaches("Records.*") + dataService.clearCaches("PlayerRecords.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
