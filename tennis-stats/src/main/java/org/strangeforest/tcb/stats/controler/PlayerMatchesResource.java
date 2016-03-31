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
public class PlayerMatchesResource {

	@Autowired private MatchesService matchesService;

	private static final int MAX_MATCHES = 10000;

	private static Map<String, String> ORDER_MAP = new TreeMap<String, String>() {{
		put("date", "date");
		put("tournament", "tournament");
		put("surface", "surface");
		put("round", "round");
	}};
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("date"), desc("round"), desc("match_num")};

	@RequestMapping("/matchesTable")
	public BootgridTable<Match> matchesTable(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "round", required = false) String round,
		@RequestParam(value = "opponent", required = false) String opponent,
		@RequestParam(value = "outcome", required = false) String outcome,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		MatchFilter filter = new MatchFilter(season, level, surface, tournamentId, tournamentEventId, round, OpponentFilter.forMatches(opponent, playerId), OutcomeFilter.forMatches(outcome, playerId), searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getPlayerMatchesTable(playerId, filter, orderBy, pageSize, current);
	}
}
