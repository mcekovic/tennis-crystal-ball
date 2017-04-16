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
public class EloRatingsJob extends DataLoadJob {

	@Autowired private DataService dataService;

	@Scheduled(cron = "${tennis-stats.jobs.compute-elo-ratings:0 20 3 * * MON}")
	public void loadNewRankings() {
		execute();
	}

	@Override protected Collection<String> params() {
		return asList("-el", "-c 3", "-f 0");
	}

	@Override protected String onSuccess() {
		int cacheCount = dataService.clearCaches("Rankings.*");
		return cacheCount + " cache(s) cleared.";
	}
}
