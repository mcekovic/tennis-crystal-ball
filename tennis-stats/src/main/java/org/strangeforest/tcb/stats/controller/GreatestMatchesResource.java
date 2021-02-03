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

	private static final Map<String, String> ORDER_MAP = Map.of(
		"date", "date",
		"tournament", "tournament",
		"surface", "surface",
		"speed", "court_speed NULLS LAST",
		"round", "round",
		"bestOf", "best_of",
		"matchScore", "match_score"
	);
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("match_score");

	@GetMapping("/greatestMatchesTable")
	public BootgridTable<Match> greatestMatchesTable(
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "bestRank", required = false) Integer bestRank,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var dateRange = DateUtil.toDateRange(fromSeason, toSeason);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = MatchFilter.forMatches(dateRange, level, bestOf, surface, indoor, speedRange, tournamentId, round, searchPhrase);

		var orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		var pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getGreatestMatchesTable(filter, bestRank, orderBy, pageSize, current);
	}
}
