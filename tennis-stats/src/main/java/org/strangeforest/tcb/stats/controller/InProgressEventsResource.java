package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.forecast.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class InProgressEventsResource {

	@Autowired private TournamentForecastService forecastService;

	private static final int MAX_TOURNAMENT_EVENTS = 5000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("date", "date")
		.put("name", "name")
		.put("surface", "surface")
		.put("draw", "draw_type, draw_size")
		.put("playerCount", "player_count")
		.put("participation", "participation")
		.put("strength", "strength")
		.put("averageEloRating", "average_elo_rating")
		.put("completed", "completed")
	.build();
	public static final OrderBy[] DEFAULT_ORDER = new OrderBy[] {desc("date"), asc("level"), asc("name")};

	@GetMapping("/inProgressEventsTable")
	public BootgridTable<InProgressEvent> inProgressEventsTable(
		@RequestParam(name = "completed", defaultValue = F) boolean completed,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat
	) {
		InProgressEventFilter filter = new InProgressEventFilter(completed, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENT_EVENTS;
		return forecastService.getInProgressEventsTable(filter, priceFormat, orderBy, pageSize, current);
	}

	static String defaultOrderBy() {
		return BootgridUtil.getOrderBy(emptyMap(), ORDER_MAP, DEFAULT_ORDER);
	}
}
