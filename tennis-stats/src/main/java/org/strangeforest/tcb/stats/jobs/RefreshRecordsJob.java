package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("jobs")
public class RefreshRecordsJob {

	@Autowired private DataLoadCommand dataLoadCommand;
	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshRecordsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.refresh-records:0 5 2 * * MON}")
	public void refreshRecords() {
		if (dataLoadCommand.execute("RefreshRecords", "-rr", "-c 1", "-rp 100") == 0) {
			try {
				dataLoadCommand.execute("Vacuum", "-vc", "-c 1");
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
