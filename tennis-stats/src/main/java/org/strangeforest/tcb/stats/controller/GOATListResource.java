package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
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
		.put("altFinals", "alt_finals")
		.put("masters", "masters")
		.put("olympics", "olympics")
		.put("bigTitles", "big_titles")
		.put("titles", "titles")
		.put("weeksAtNo1", "weeks_at_no1")
		.put("wonPct", "matches_won::REAL / (matches_won + matches_lost)")
		.put("bestEloRating", "best_elo_rating NULLS LAST")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("goat_points"), desc("grand_slams"), desc("tour_finals"), desc("masters"), desc("titles"), asc("name")};

	@GetMapping("/goatListTable")
	public BootgridTable<GOATListRow> goatListTable(
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "oldLegends", defaultValue = T) boolean oldLegends,
		@RequestParam(name = "extrapolate", defaultValue = F) boolean extrapolate,
		@RequestParam(name = "tournamentPointsFactor", defaultValue = "1") int tournamentPointsFactor,
		@RequestParam(name = "rankingPointsFactor", defaultValue = "1") int rankingPointsFactor,
		@RequestParam(name = "achievementsPointsFactor", defaultValue = "1") int achievementsPointsFactor,
		@RequestParam(name = "levelGPointsFactor", defaultValue = "1") int levelGPointsFactor,
		@RequestParam(name = "levelFPointsFactor", defaultValue = "1") int levelFPointsFactor,
		@RequestParam(name = "levelLPointsFactor", defaultValue = "1") int levelLPointsFactor,
		@RequestParam(name = "levelMPointsFactor", defaultValue = "1") int levelMPointsFactor,
		@RequestParam(name = "levelOPointsFactor", defaultValue = "1") int levelOPointsFactor,
		@RequestParam(name = "levelAPointsFactor", defaultValue = "1") int levelAPointsFactor,
		@RequestParam(name = "levelBPointsFactor", defaultValue = "1") int levelBPointsFactor,
		@RequestParam(name = "levelDPointsFactor", defaultValue = "1") int levelDPointsFactor,
		@RequestParam(name = "levelTPointsFactor", defaultValue = "1") int levelTPointsFactor,
		@RequestParam(name = "yearEndRankPointsFactor", defaultValue = "1") int yearEndRankPointsFactor,
		@RequestParam(name = "bestRankPointsFactor", defaultValue = "1") int bestRankPointsFactor,
		@RequestParam(name = "weeksAtNo1PointsFactor", defaultValue = "1") int weeksAtNo1PointsFactor,
		@RequestParam(name = "weeksAtEloTopNPointsFactor", defaultValue = "1") int weeksAtEloTopNPointsFactor,
		@RequestParam(name = "bestEloRatingPointsFactor", defaultValue = "1") int bestEloRatingPointsFactor,
		@RequestParam(name = "grandSlamPointsFactor", defaultValue = "1") int grandSlamPointsFactor,
		@RequestParam(name = "bigWinsPointsFactor", defaultValue = "1") int bigWinsPointsFactor,
		@RequestParam(name = "h2hPointsFactor", defaultValue = "1") int h2hPointsFactor,
		@RequestParam(name = "recordsPointsFactor", defaultValue = "1") int recordsPointsFactor,
		@RequestParam(name = "bestSeasonPointsFactor", defaultValue = "1") int bestSeasonPointsFactor,
		@RequestParam(name = "greatestRivalriesPointsFactor", defaultValue = "1") int greatestRivalriesPointsFactor,
		@RequestParam(name = "performancePointsFactor", defaultValue = "1") int performancePointsFactor,
		@RequestParam(name = "statisticsPointsFactor", defaultValue = "1") int statisticsPointsFactor,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue = "") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(active, searchPhrase);
		Map<String, Integer> levelPointsFactors = ImmutableMap.<String, Integer>builder()
			.put("levelGPointsFactor", levelGPointsFactor)
			.put("levelFPointsFactor", levelFPointsFactor)
			.put("levelLPointsFactor", levelLPointsFactor)
			.put("levelMPointsFactor", levelMPointsFactor)
			.put("levelOPointsFactor", levelOPointsFactor)
			.put("levelAPointsFactor", levelAPointsFactor)
			.put("levelBPointsFactor", levelBPointsFactor)
			.put("levelDPointsFactor", levelDPointsFactor)
			.put("levelTPointsFactor", levelTPointsFactor)
		.build();
		GOATListConfig config = new GOATListConfig(
			oldLegends, extrapolate, tournamentPointsFactor, rankingPointsFactor, achievementsPointsFactor, levelPointsFactors,
			yearEndRankPointsFactor, bestRankPointsFactor, weeksAtNo1PointsFactor, weeksAtEloTopNPointsFactor, bestEloRatingPointsFactor,
			grandSlamPointsFactor, bigWinsPointsFactor, h2hPointsFactor, recordsPointsFactor, bestSeasonPointsFactor, greatestRivalriesPointsFactor, performancePointsFactor, statisticsPointsFactor
		);
		int playerCount = goatListService.getPlayerCount(filter, config);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return goatListService.getGOATListTable(playerCount, filter, config, orderBy, pageSize, current);
	}
}
