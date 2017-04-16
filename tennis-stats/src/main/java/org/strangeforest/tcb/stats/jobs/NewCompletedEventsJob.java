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
public class NewCompletedEventsJob extends DataLoadJob {

	@Autowired private DataService dataService;

	@Scheduled(cron = "${tennis-stats.jobs.load-new-completed-events:0 10 * * * *}")
	public void loadNewCompletedEvents() {
		execute();
	}

	@Override protected Collection<String> params() {
		return asList("-nt", "-c 1");
	}

	@Override protected String onSuccess() {
		dataService.evictGlobal("Tournaments");
		int cacheCount = dataService.clearCaches("Tournament.*");
		return cacheCount + " cache(s) cleared.";
	}
}
