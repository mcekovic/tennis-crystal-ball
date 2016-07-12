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
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		TournamentEventResultFilter filter = new TournamentEventResultFilter(season, level, surface, tournamentId, result, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return tournamentService.getPlayerTournamentEventResultsTable(playerId, filter, orderBy, pageSize, current);
	}
}
