package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class GOATListResource {

	@Autowired private GOATListService goatListService;

	private static Map<String, String> ORDER_MAP = new TreeMap<String, String>() {{
		put("goatPoints", "goat_points");
		put("tournamentGoatPoints", "tournament_goat_points");
		put("rankingGoatPoints", "ranking_goat_points");
		put("achievementsGoatPoints", "achievements_goat_points");
		put("grandSlams", "grand_slams");
		put("tourFinals", "tour_finals");
		put("masters", "masters");
		put("olympics", "olympics");
		put("bigTitles", "big_titles");
		put("titles", "titles");
		put("bestEloRating", "best_elo_rating NULLS LAST");
	}};
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("goat_points"), asc("name")};

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "active", required = false) Boolean active,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(active, searchPhrase);
		int playerCount = goatListService.getPlayerCount(filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return goatListService.getGOATListTable(playerCount, filter, orderBy, pageSize, current);
	}
}
