package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

@RestController
public class StatsLeadersResource {

	@Autowired private StatsLeadersService statsLeadersService;

	private static Map<String, String> ORDER_MAP = Collections.singletonMap("value", "value");
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("name");

	@RequestMapping("/statsLeadersTable")
	public BootgridTable<StatsLeaderRow> statsLeadersTable(
		@RequestParam(value = "dimension") String dimension,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		int playerCount = statsLeadersService.getPlayerCount(dimension, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return statsLeadersService.getStatsLeadersTable(dimension, playerCount, filter, orderBy, pageSize, current);
	}

	@RequestMapping("/statsLeadersMinEntries")
	public String topPerformersDimension(
		@RequestParam(value = "dimension") String dimension
	) {
		return statsLeadersService.getStatsLeadersMinEntries(dimension);
	}
}
