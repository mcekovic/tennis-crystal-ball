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
public class SeasonsResource {

	@Autowired private SeasonsService seasonsService;

	private static final int MAX_SEASONS = 100;

	private static Map<String, String> SEASONS_ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("season", "season")
		.put("tournamentCount", "tournament_count")
		.put("grandSlamCount", "grand_slam_count")
		.put("tourFinalsCount", "tour_finals_count")
		.put("mastersCount", "masters_count")
		.put("olympicsCount", "olympics_count")
		.put("atp500Count", "atp500_count")
		.put("atp250Count", "atp250_count")
		.put("hardCount", "hard_count")
		.put("clayCount", "clay_count")
		.put("grassCount", "grass_count")
		.put("carpetCount", "carpet_count")
		.put("outdoorCount", "outdoor_count")
		.put("indoorCount", "indoor_count")
		.put("matchCount", "match_count")
		.put("speed", "court_speed NULLS LAST")
	.build();
	private static final OrderBy SEASONS_DEFAULT_ORDER = OrderBy.desc("season");

	private static Map<String, String> BEST_SEASONS_ORDER_MAP = ImmutableMap.<String, String>builder()
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
		.put("wonPct", "matches_won_pct")
		.put("yearEndRank", "year_end_rank")
		.put("bestEloRating", "best_elo_rating")
	.build();
	private static final OrderBy BEST_SEASONS_DEFAULT_ORDER = OrderBy.asc("season_rank");

	@GetMapping("/seasonsTable")
	public BootgridTable<Season> seasonsTable(
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		String orderBy = BootgridUtil.getOrderBy(requestParams, SEASONS_ORDER_MAP, SEASONS_DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_SEASONS;
		return seasonsService.getSeasons(orderBy, pageSize, current);
	}

	@GetMapping("/bestSeasonsTable")
	public BootgridTable<BestSeasonRow> bestSeasonsTable(
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		int seasonCount = seasonsService.getBestSeasonCount(surface, filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, BEST_SEASONS_ORDER_MAP, BEST_SEASONS_DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : seasonCount;
		return seasonsService.getBestSeasonsTable(seasonCount, surface, filter, orderBy, pageSize, current);
	}

	@GetMapping("/bestSeasonsMinGOATPoints")
	public int bestSeasonsMinGOATPoints(
		@RequestParam(name = "surface", required = false) String surface
	) {
		return seasonsService.getMinSeasonGOATPoints(surface);
	}
}
