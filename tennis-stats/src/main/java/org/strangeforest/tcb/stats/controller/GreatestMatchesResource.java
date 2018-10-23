package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

@RestController
public class GreatestMatchesResource {

	@Autowired private MatchesService matchesService;

	private static final int MAX_MATCHES = 1000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("date", "date")
		.put("tournament", "tournament")
		.put("surface", "surface")
		.put("speed", "court_speed NULLS LAST")
		.put("round", "round")
		.put("bestOf", "best_of")
		.put("matchScore", "match_score")
	.build();
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("match_score");

	@GetMapping("/greatestMatchesTable")
	public BootgridTable<Match> greatestMatchesTable(
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "bestRank", required = false) Integer bestRank,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		Range<LocalDate> dateRange = DateUtil.toDateRange(fromSeason, toSeason);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		MatchFilter filter = MatchFilter.forMatches(dateRange, level, bestOf, surface, indoor, speedRange, tournamentId, round, searchPhrase);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getGreatestMatchesTable(filter, bestRank, orderBy, pageSize, current);
	}
}
