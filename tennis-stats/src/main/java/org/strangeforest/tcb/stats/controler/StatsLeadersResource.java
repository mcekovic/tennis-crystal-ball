package org.strangeforest.tcb.stats.controler;

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

	@RequestMapping("/statsLeadersTable")
	public BootgridTable<StatsLeaderRow> statsLeadersTable(
		@RequestParam(value = "category") String category,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		StatsPlayerListFilter filter = new StatsPlayerListFilter(searchPhrase, season, surface, tournamentId, tournamentEventId);
		int playerCount = statsLeadersService.getPlayerCount(category, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return statsLeadersService.getStatsLeadersTable(category, playerCount, filter, orderBy, pageSize, current);
	}

	@RequestMapping("/statsLeadersMinEntries")
	public String statsLeadersMinEntries(
		@RequestParam(value = "category") String category,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		StatsPlayerListFilter filter = new StatsPlayerListFilter(season, surface, tournamentId, tournamentEventId);
		return statsLeadersService.getStatsLeadersMinEntries(category, filter);
	}
}
