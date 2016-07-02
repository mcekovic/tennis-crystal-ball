package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

@RestController
public class GreatestRivalriesResource {

	@Autowired private RivalriesService rivalriesService;

	private static final int MAX_RIVALRIES = 1000;

	private static Map<String, String> ORDER_MAP = new TreeMap<String, String>() {{
		put("matches", "matches");
		put("won", "won");
		put("lost", "lost");
		put("wonPctStr", "won::real/(won + lost)");
	}};
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("rivalry_rank");

	@RequestMapping("/greatestRivalriesTable")
	public BootgridTable<GreatestRivalry> greatestRivalriesTable(
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "round", required = false) String round,
		@RequestParam(value = "bestRank", required = false) Integer bestRank,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		RivalryFilter filter = new RivalryFilter(Range.all(), level, surface, round);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getGreatestRivalriesTable(filter, bestRank, orderBy, pageSize, current);
	}

	@RequestMapping("/greatestRivalriesMinMatches")
	public int greatestRivalriesMinMatches(
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "round", required = false) String round
	) {
		RivalryFilter filter = new RivalryFilter(Range.all(), level, surface, round);
		return rivalriesService.getGreatestRivalriesMinMatches(filter);
	}
}
