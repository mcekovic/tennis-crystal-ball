package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

@RestController
public class PlayerTournamentsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENTS = 1000;

	private static Map<String, String> ORDER_MAP = new TreeMap<String, String>() {{
		put("season", "season");
		put("date", "date");
		put("name", "name");
		put("surface", "surface");
		put("draw", "draw_type, draw_size");
		put("result", "result");
	}};
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("date");

	@RequestMapping("/playerTournamentsTable")
	public BootgridTable<PlayerTournamentEvent> playerTournamentsTable(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "result", required = false) String result,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		TournamentEventResultFilter filter = new TournamentEventResultFilter(season, level, surface, tournamentId, result, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return tournamentService.getPlayerTournamentEventResultsTable(playerId, filter, orderBy, pageSize, current);
	}
}
