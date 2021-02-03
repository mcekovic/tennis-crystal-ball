package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static java.util.Map.*;

@RestController
public class SeasonsResource {

	@Autowired private SeasonsService seasonsService;

	private static final int MAX_SEASONS = 100;

	private static final Map<String, String> SEASONS_ORDER_MAP = Map.ofEntries(
		entry("season", "season"),
		entry("tournamentCount", "tournament_count"),
		entry("grandSlamCount", "grand_slam_count"),
		entry("tourFinalsCount", "tour_finals_count"),
		entry("mastersCount", "masters_count"),
		entry("olympicsCount", "olympics_count"),
		entry("atp500Count", "atp500_count"),
		entry("atp250Count", "atp250_count"),
		entry("hardCount", "hard_count"),
		entry("clayCount", "clay_count"),
		entry("grassCount", "grass_count"),
		entry("carpetCount", "carpet_count"),
		entry("outdoorCount", "outdoor_count"),
		entry("indoorCount", "indoor_count"),
		entry("matchCount", "match_count"),
		entry("speed", "court_speed NULLS LAST")
	);
	private static final OrderBy SEASONS_DEFAULT_ORDER = OrderBy.desc("season");

	private static final Map<String, String> BEST_SEASONS_ORDER_MAP = Map.ofEntries(
		entry("season", "season"),
		entry("goatPoints", "goat_points"),
		entry("grandSlamTitles", "grand_slam_titles"),
		entry("grandSlamFinals", "grand_slam_finals"),
		entry("grandSlamSemiFinals", "grand_slam_semi_finals"),
		entry("tourFinalsTitles", "tour_finals_titles"),
		entry("tourFinalsFinals", "tour_finals_finals"),
		entry("mastersTitles", "masters_titles"),
		entry("mastersFinals", "masters_finals"),
		entry("olympicsTitles", "olympics_titles"),
		entry("olympicsFinals", "olympics_finals"),
		entry("titles", "titles"),
		entry("wonPct", "matches_won_pct"),
		entry("yearEndRank", "year_end_rank"),
		entry("bestEloRating", "best_elo_rating")
	);
	private static final OrderBy BEST_SEASONS_DEFAULT_ORDER = OrderBy.asc("season_rank");

	@GetMapping("/seasonsTable")
	public BootgridTable<Season> seasonsTable(
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam Map<String, String> requestParams
	) {
		var orderBy = BootgridUtil.getOrderBy(requestParams, SEASONS_ORDER_MAP, SEASONS_DEFAULT_ORDER);
		var pageSize = rowCount > 0 ? rowCount : MAX_SEASONS;
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
		var filter = new PlayerListFilter(searchPhrase);
		var seasonCount = seasonsService.getBestSeasonCount(surface, filter);

		var orderBy = BootgridUtil.getOrderBy(requestParams, BEST_SEASONS_ORDER_MAP, BEST_SEASONS_DEFAULT_ORDER);
		var pageSize = rowCount > 0 ? rowCount : seasonCount;
		return seasonsService.getBestSeasonsTable(seasonCount, surface, filter, orderBy, pageSize, current);
	}

	@GetMapping("/bestSeasonsMinGOATPoints")
	public int bestSeasonsMinGOATPoints(
		@RequestParam(name = "surface", required = false) String surface
	) {
		return seasonsService.getMinSeasonGOATPoints(surface);
	}
}
