package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
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
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "bestRank", required = false) Integer bestRank,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		RivalryFilter filter = new RivalryFilter(RangeUtil.toRange(season, season), level, surface, round);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getGreatestRivalriesTable(filter, bestRank, orderBy, pageSize, current);
	}

	@GetMapping("/greatestRivalriesMinMatches")
	public int greatestRivalriesMinMatches(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round
	) {
		RivalryFilter filter = new RivalryFilter(RangeUtil.toRange(season, season), level, surface, round);
		return rivalriesService.getGreatestRivalriesMinMatches(filter);
	}
}
