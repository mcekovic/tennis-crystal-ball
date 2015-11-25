package org.strangeforest.tcb.stats.controler;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

@RestController
public class GreatestRivalriesResource {

	@Autowired private RivalriesService rivalriesService;

	private static final int MAX_RIVALRIES = 1000;

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("matches", "matches");
		ORDER_MAP.put("won", "won");
		ORDER_MAP.put("lost", "lost");
		ORDER_MAP.put("wonPctStr", "won::real/(won + lost)");
	}
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("rivalry_rank");

	@RequestMapping("/greatestRivalriesTable")
	public BootgridTable<GreatestRivalry> greatestRivalriesTable(
		@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate fromDate,
		@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate toDate,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		RivalryFilter filter = new RivalryFilter(DateUtil.toRange(fromDate, toDate), level, surface);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getGreatestRivalriesTable(filter, orderBy, pageSize, current);
	}

	@RequestMapping("/greatestRivalriesMinMatches")
	public int greatestRivalriesMinMatches(
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface
	) {
		RivalryFilter filter = new RivalryFilter(Range.all(), level, surface);
		return rivalriesService.getGreatestRivalriesMinMatches(filter);
	}
}
