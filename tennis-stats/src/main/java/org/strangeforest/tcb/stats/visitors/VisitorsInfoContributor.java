package org.strangeforest.tcb.stats.visitors;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.visitors.VisitorInterval.*;
import static org.strangeforest.tcb.stats.visitors.VisitorStat.*;

@Component @VisitorSupport
public class VisitorsInfoContributor implements InfoContributor {

	@Autowired private VisitorRepository repository;

	@Override public void contribute(Info.Builder builder) {
		builder.withDetail("visitors", ImmutableMap.of(
			"active", repository.getVisitors(ACTIVE_VISITORS, HOUR, false),
			"lastDayVisits", repository.getVisitors(VISITS, DAY, false),
			"lastDayHits", repository.getVisitors(HITS, DAY, false),
			"activeByCountry", repository.getVisitorsByCountry(ACTIVE_VISITORS, HOUR, false),
			"lastDayVisitsByAgentType", repository.getVisitorsByAgentType(VISITS, DAY, false)
		));
	}
}
