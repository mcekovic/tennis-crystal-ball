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
	@Autowired private MatchesService matchesService;

	private static Map<String, String> ORDER_MAP = Collections.singletonMap("value", "value");
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("name");

	@GetMapping("/statsLeadersTable")
	public BootgridTable<StatsLeaderRow> statsLeadersTable(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minEntries", required = false) Integer minEntries,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PerfStatsFilter filter = new PerfStatsFilter(active, searchPhrase, season, level, surface, round, tournamentId, tournamentEventId, opponentFilter);
		int playerCount = statsLeadersService.getPlayerCount(category, filter, minEntries);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return statsLeadersService.getStatsLeadersTable(category, playerCount, filter, minEntries, orderBy, pageSize, current);
	}

	@GetMapping("/statsLeadersMinEntries")
	public String statsLeadersMinEntries(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minEntries", required = false) Integer minEntries
	) {
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PerfStatsFilter filter = new PerfStatsFilter(season, level, surface, round, tournamentId, tournamentEventId, opponentFilter);
		return statsLeadersService.getStatsLeadersMinEntries(category, filter, minEntries);
	}
}
