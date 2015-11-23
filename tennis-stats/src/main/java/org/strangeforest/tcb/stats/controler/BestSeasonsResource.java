package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

@RestController
public class BestSeasonsResource {

	@Autowired private BestSeasonsService bestSeasonsService;

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("season", "season");
		ORDER_MAP.put("goatPoints", "goat_points");
		ORDER_MAP.put("grandSlamTitles", "grand_slam_titles");
		ORDER_MAP.put("grandSlamFinals", "grand_slam_finals");
		ORDER_MAP.put("grandSlamSemiFinals", "grand_slam_semi_finals");
		ORDER_MAP.put("tourFinalsTitles", "tour_finals_titles");
		ORDER_MAP.put("tourFinalsFinals", "tour_finals_finals");
		ORDER_MAP.put("mastersTitles", "masters_titles");
		ORDER_MAP.put("mastersFinals", "masters_finals");
		ORDER_MAP.put("olympicsTitles", "olympics_titles");
		ORDER_MAP.put("olympicsFinals", "olympics_finals");
		ORDER_MAP.put("titles", "titles");
	}
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("season_rank");

	@RequestMapping("/bestSeasonsTable")
	public BootgridTable<BestSeasonRow> bestSeasonsTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		int seasonCount = bestSeasonsService.getBestSeasonCount(filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : seasonCount;
		return bestSeasonsService.getBestSeasonsTable(seasonCount, filter, orderBy, pageSize, current);
	}
}
