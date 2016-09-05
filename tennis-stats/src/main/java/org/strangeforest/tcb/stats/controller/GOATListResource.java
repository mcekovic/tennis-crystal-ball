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
public class GOATListResource {

	@Autowired private GOATListService goatListService;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("goatPoints", "goat_points")
		.put("tournamentGoatPoints", "tournament_goat_points")
		.put("rankingGoatPoints", "ranking_goat_points")
		.put("achievementsGoatPoints", "achievements_goat_points")
		.put("grandSlams", "grand_slams")
		.put("tourFinals", "tour_finals")
		.put("masters", "masters")
		.put("olympics", "olympics")
		.put("bigTitles", "big_titles")
		.put("titles", "titles")
		.put("bestEloRating", "best_elo_rating NULLS LAST")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("goat_points"), asc("name")};

	@GetMapping("/goatListTable")
	public BootgridTable<GOATListRow> goatListTable(
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue = "") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(active, searchPhrase);
		int playerCount = goatListService.getPlayerCount(filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return goatListService.getGOATListTable(playerCount, filter, orderBy, pageSize, current);
	}
}
