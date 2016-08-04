package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

@RestController
public class BestSeasonsResource {

	@Autowired private BestSeasonsService bestSeasonsService;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("season", "season")
		.put("goatPoints", "goat_points")
		.put("grandSlamTitles", "grand_slam_titles")
		.put("grandSlamFinals", "grand_slam_finals")
		.put("grandSlamSemiFinals", "grand_slam_semi_finals")
		.put("tourFinalsTitles", "tour_finals_titles")
		.put("tourFinalsFinals", "tour_finals_finals")
		.put("mastersTitles", "masters_titles")
		.put("mastersFinals", "masters_finals")
		.put("olympicsTitles", "olympics_titles")
		.put("olympicsFinals", "olympics_finals")
		.put("titles", "titles")
		.put("yearEndRank", "year_end_rank")
	.build();
	private static final OrderBy DEFAULT_ORDER = OrderBy.asc("season_rank");

	@RequestMapping("/bestSeasonsTable")
	public BootgridTable<BestSeasonRow> bestSeasonsTable(
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		int seasonCount = bestSeasonsService.getBestSeasonCount(filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : seasonCount;
		return bestSeasonsService.getBestSeasonsTable(seasonCount, filter, orderBy, pageSize, current);
	}
}
