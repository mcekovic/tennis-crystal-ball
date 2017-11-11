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
public class TournamentEventsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENT_EVENTS = 5000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("date", "date")
		.put("name", "name")
		.put("surface", "surface")
		.put("draw", "draw_type, draw_size")
		.put("playerCount", "player_count")
		.put("participation", "participation")
		.put("strength", "strength")
		.put("averageEloRating", "average_elo_rating")
	.build();
	private static final OrderBy[] DEFAULT_ORDER = new OrderBy[] {desc("date"), asc("level"), asc("name")};

	@GetMapping("/tournamentEventsTable")
	public BootgridTable<TournamentEvent> tournamentEventsTable(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		TournamentEventFilter filter = new TournamentEventFilter(season, null, level, surface, indoor, tournamentId, null, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENT_EVENTS;
		return tournamentService.getTournamentEventsTable(filter, orderBy, pageSize, current);
	}
}
