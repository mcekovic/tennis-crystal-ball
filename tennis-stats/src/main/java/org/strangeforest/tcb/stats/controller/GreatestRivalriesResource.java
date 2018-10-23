package org.strangeforest.tcb.stats.controller;

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
public class GreatestRivalriesResource {

	@Autowired private RivalriesService rivalriesService;

	private static final int MAX_RIVALRIES = 1000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("matches", "matches")
		.put("rivalryScore", "rivalry_score")
	.build();
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("rivalry_rank");

	@GetMapping("/greatestRivalriesTable")
	public BootgridTable<GreatestRivalry> greatestRivalriesTable(
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
		@RequestParam(name = "minMatches", required = false) Integer minMatches,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		RivalryFilter filter = new RivalryFilter(seasonRange, level, bestOf, surface, indoor, speedRange, round, tournamentId);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getGreatestRivalriesTable(filter, bestRank, minMatches, orderBy, pageSize, current);
	}

	@GetMapping("/greatestRivalriesMinMatches")
	public int greatestRivalriesMinMatches(
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
		@RequestParam(name = "minMatches", required = false) Integer minMatches
	) {
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		RivalryFilter filter = new RivalryFilter(seasonRange, level, bestOf, surface, indoor, speedRange, round, tournamentId);
		return rivalriesService.getGreatestRivalriesMinMatches(filter, bestRank, minMatches);
	}
}
