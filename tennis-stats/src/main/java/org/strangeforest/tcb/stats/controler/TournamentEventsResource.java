package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

@RestController
public class TournamentEventsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENT_EVENTS = 1000;

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("date", "date");
		ORDER_MAP.put("name", "name");
		ORDER_MAP.put("surface", "surface");
		ORDER_MAP.put("draw", "draw_type, draw_size");
		ORDER_MAP.put("playerCount", "player_count NULLS LAST");
		ORDER_MAP.put("participationPoints", "participation_points NULLS LAST");
		ORDER_MAP.put("participationPct", "participation_points::DECIMAL/max_participation_points NULLS LAST");
	}
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("date");

	@RequestMapping("/tournamentEventsTable")
	public BootgridTable<TournamentEvent> playerTournamentsTable(
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		TournamentEventFilter filter = new TournamentEventFilter(season, level, surface, tournamentId, null, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENT_EVENTS;
		return tournamentService.getTournamentEventsTable(filter, orderBy, pageSize, current);
	}
}
