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
public class PlayerMatchesResource {

	@Autowired private MatchesService matchesService;

	private static final int MAX_MATCHES = 10000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("date", "date")
		.put("tournament", "tournament")
		.put("surface", "surface")
		.put("round", "round")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("date"), desc("round"), desc("match_num")};

	@GetMapping("/matchesTable")
	public BootgridTable<Match> matchesTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		OpponentFilter opponentFilter = OpponentFilter.forMatches(opponent);
		OutcomeFilter outcomeFilter = OutcomeFilter.forMatches(outcome);
		StatsFilter statsFilter = new StatsFilter(statsCategory, statsFrom, statsTo);
		MatchFilter filter = MatchFilter.forMatches(season, level, surface, tournamentId, tournamentEventId, round, opponentFilter, outcomeFilter, statsFilter, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getPlayerMatchesTable(playerId, filter, orderBy, pageSize, current);
	}
}
