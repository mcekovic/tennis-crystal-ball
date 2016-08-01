package org.strangeforest.tcb.stats.controler;

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

	@RequestMapping("/matchesTable")
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
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		MatchFilter filter = new MatchFilter(season, level, surface, tournamentId, tournamentEventId, round, OpponentFilter.forMatches(opponent), OutcomeFilter.forMatches(outcome), searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getPlayerMatchesTable(playerId, filter, orderBy, pageSize, current);
	}
}
