package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

@RestController
public class StatsLeadersResource {

	@Autowired private StatsLeadersService statsLeadersService;

	private static Map<String, String> ORDER_MAP = Collections.singletonMap("value", "value");
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("name");

	@GetMapping("/statsLeadersTable")
	public BootgridTable<StatsLeaderRow> statsLeadersTable(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		StatsPerfFilter filter = new StatsPerfFilter(active, searchPhrase, season, null, surface, null, tournamentId, tournamentEventId, null);
		int playerCount = statsLeadersService.getPlayerCount(category, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return statsLeadersService.getStatsLeadersTable(category, playerCount, filter, orderBy, pageSize, current);
	}

	@GetMapping("/statsLeadersMinEntries")
	public String statsLeadersMinEntries(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		StatsPerfFilter filter = new StatsPerfFilter(season, surface, tournamentId, tournamentEventId);
		return statsLeadersService.getStatsLeadersMinEntries(category, filter);
	}
}
