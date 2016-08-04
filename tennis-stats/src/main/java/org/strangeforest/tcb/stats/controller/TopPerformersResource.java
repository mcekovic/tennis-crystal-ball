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

	@RequestMapping("/topPerformersTable")
	public BootgridTable<TopPerformerRow> topPerformersTable(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		StatsPlayerListFilter filter = new StatsPlayerListFilter(active, searchPhrase, season);
		int playerCount = topPerformersService.getPlayerCount(category, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return topPerformersService.getTopPerformersTable(category, playerCount, filter, orderBy, pageSize, current);
	}

	@RequestMapping("/topPerformersMinEntries")
	public String topPerformersMinEntries(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season
	) {
		StatsPlayerListFilter filter = new StatsPlayerListFilter(season);
		return topPerformersService.getTopPerformersMinEntries(category, filter);
	}
}
