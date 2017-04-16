package org.strangeforest.tcb.stats.jobs;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.Arrays.*;

@Component
@Profile("openshift")
public class NewRankingsJob extends DataLoadJob {

	@Autowired private DataService dataService;

	@Scheduled(cron = "${tennis-stats.jobs.load-new-rankings:0 15 3 * * MON}")
	public void loadNewRankings() {
		execute();
	}

	@Override protected Collection<String> params() {
		return asList("-nr", "-c 1");
	}

	@Override protected String onSuccess() {
		int cacheCount = dataService.clearCaches("Rankings.*");
		return cacheCount + " cache(s) cleared.";
	}
}
