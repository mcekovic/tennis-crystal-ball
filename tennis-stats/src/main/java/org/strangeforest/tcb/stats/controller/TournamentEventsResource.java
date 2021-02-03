package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class TournamentEventsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENT_EVENTS = 5000;

	private static final Map<String, String> ORDER_MAP = Map.of(
		"date", "date",
		"name", "name",
		"surface", "surface",
		"speed", "court_speed NULLS LAST",
		"draw", "draw_type, draw_size",
		"playerCount", "player_count",
		"participation", "participation",
		"strength", "strength",
		"averageEloRating", "average_elo_rating"
	);
	private static final OrderBy[] DEFAULT_ORDER = new OrderBy[] {desc("date"), asc("level"), asc("name")};

	@GetMapping("/tournamentEventsTable")
	public BootgridTable<TournamentEvent> tournamentEventsTable(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = new TournamentEventFilter(season, null, level, surface, indoor, speedRange, tournamentId, null, searchPhrase);
		var orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		var pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENT_EVENTS;
		return tournamentService.getTournamentEventsTable(filter, orderBy, pageSize, current);
	}
}
