package org.strangeforest.tcb.stats.web;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.web.VisitorInterval.*;
import static org.strangeforest.tcb.stats.web.VisitorStat.*;

@Component @VisitorSupport
public class VisitorsInfoContributor implements InfoContributor {

	@Autowired private VisitorRepository repository;

	@Override public void contribute(Info.Builder builder) {
		builder.withDetail("visitors", ImmutableMap.of(
			"active", repository.getVisitors(ACTIVE_VISITORS, HOUR),
			"lastDayVisits", repository.getVisitors(VISITS, DAY),
			"lastDayHits", repository.getVisitors(HITS, DAY)
		));
	}
}
