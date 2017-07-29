package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class PlayerRivalriesResource {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private StatisticsService statisticsService;

	private static final int MAX_RIVALRIES = 1000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("bestRank", "best_rank")
		.put("matches", "matches")
		.put("won", "won")
		.put("lost", "lost")
		.put("wonPctStr", "CASE WHEN won + lost > 0 THEN won::REAL / (won + lost) ELSE 0 END")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("matches"), desc("won")};

	@GetMapping("/playerRivalriesTable")
	public BootgridTable<PlayerRivalryRow> playerRivalriesTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		RivalryFilter rivalryFilter = new RivalryFilter(RangeUtil.toRange(season, season), level, surface, round);
		RivalryPlayerListFilter filter = new RivalryPlayerListFilter(searchPhrase, rivalryFilter);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getPlayerRivalriesTable(playerId, filter, orderBy, pageSize, current);
	}

	@GetMapping("/h2h")
	public List<Integer> h2h(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "outcome", required = false) String outcome
	) {
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, season, level, surface, tournamentId, round, outcome, score));
		return asList(stats1.getMatchesWon(), stats1.getMatchesLost());
	}
}
