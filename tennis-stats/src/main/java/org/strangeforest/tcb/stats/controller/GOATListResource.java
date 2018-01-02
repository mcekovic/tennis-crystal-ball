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
	@Autowired private MatchesService matchesService;

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
		.put("wonPct", "matches_won_pct")
		.put("bestEloRating", "best_elo_rating")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("goat_points"), desc("grand_slams"), desc("tour_finals"), desc("masters"), desc("titles"), asc("name")};

	@GetMapping("/goatListTable")
	public BootgridTable<GOATListRow> goatListTable(
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "oldLegends", defaultValue = T) boolean oldLegends,
		@RequestParam(name = "extrapolate", defaultValue = F) boolean extrapolate,
		@RequestParam(name = "tournamentFactor", defaultValue = "1") int tournamentFactor,
		@RequestParam(name = "rankingFactor", defaultValue = "1") int rankingFactor,
		@RequestParam(name = "achievementsFactor", defaultValue = "1") int achievementsFactor,
		@RequestParam(name = "levelFactors", defaultValue = "") String levelFactors,
		@RequestParam(name = "resultFactors", defaultValue = "") String resultFactors,
		@RequestParam(name = "yearEndRankFactor", defaultValue = "1") int yearEndRankFactor,
		@RequestParam(name = "bestRankFactor", defaultValue = "1") int bestRankFactor,
		@RequestParam(name = "weeksAtNo1Factor", defaultValue = "1") int weeksAtNo1Factor,
		@RequestParam(name = "weeksAtEloTopNFactor", defaultValue = "1") int weeksAtEloTopNFactor,
		@RequestParam(name = "bestEloRatingFactor", defaultValue = "1") int bestEloRatingFactor,
		@RequestParam(name = "grandSlamFactor", defaultValue = "1") int grandSlamFactor,
		@RequestParam(name = "bigWinsFactor", defaultValue = "1") int bigWinsFactor,
		@RequestParam(name = "h2hFactor", defaultValue = "1") int h2hFactor,
		@RequestParam(name = "recordsFactor", defaultValue = "1") int recordsFactor,
		@RequestParam(name = "bestSeasonFactor", defaultValue = "1") int bestSeasonFactor,
		@RequestParam(name = "greatestRivalriesFactor", defaultValue = "1") int greatestRivalriesFactor,
		@RequestParam(name = "performanceFactor", defaultValue = "1") int performanceFactor,
		@RequestParam(name = "statisticsFactor", defaultValue = "1") int statisticsFactor,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue = "") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(active, matchesService.getSameCountryIds(countryId), searchPhrase);
		GOATListConfig config = new GOATListConfig(
			oldLegends, extrapolate, tournamentFactor, rankingFactor, achievementsFactor, parseIntProperties(levelFactors), parseIntProperties(resultFactors),
			yearEndRankFactor, bestRankFactor, weeksAtNo1Factor, weeksAtEloTopNFactor, bestEloRatingFactor,
			grandSlamFactor, bigWinsFactor, h2hFactor, recordsFactor, bestSeasonFactor, greatestRivalriesFactor, performanceFactor, statisticsFactor
		);
		int playerCount = goatListService.getPlayerCount(surface, filter, config);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return goatListService.getGOATListTable(playerCount, surface, filter, config, orderBy, pageSize, current);
	}
}
