package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class TopPerformersResource {

	@Autowired private TopPerformersService topPerformersService;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("wonLostPct", "won_lost_pct")
		.put("won", "won")
		.put("lost", "lost")
		.put("played", "played")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("won"), asc("lost"), asc("name")};

	@GetMapping("/topPerformersTable")
	public BootgridTable<TopPerformerRow> topPerformersTable(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		StatsPerfFilter filter = new StatsPerfFilter(active, searchPhrase, season, level, surface, round, tournamentId, null, null);
		int playerCount = topPerformersService.getPlayerCount(category, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return topPerformersService.getTopPerformersTable(category, playerCount, filter, orderBy, pageSize, current);
	}

	@GetMapping("/topPerformersMinEntries")
	public String topPerformersMinEntries(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
	) {
		StatsPerfFilter filter = new StatsPerfFilter(season, level, surface, tournamentId, null, null);
		return topPerformersService.getTopPerformersMinEntries(category, filter);
	}
}
