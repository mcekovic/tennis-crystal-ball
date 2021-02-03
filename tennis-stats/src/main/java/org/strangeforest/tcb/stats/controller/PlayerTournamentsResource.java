package org.strangeforest.tcb.stats.controller;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.collect.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.util.BootgridUtil.*;
import static org.strangeforest.tcb.util.CompareUtil.*;

@RestController
public class PlayerTournamentsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENTS = 1000;

	private static final Comparator<PlayerTournament> BY_NAME = nullsLast(comparing(PlayerTournament::getName));
	private static final Comparator<PlayerTournament> BY_LEVEL = (t1, t2) -> compareLists(mapSortList(t1.getLevels(), TournamentLevel::decode), mapSortList(t2.getLevels(), TournamentLevel::decode));
	private static Map<String, Comparator<PlayerTournament>> ORDER_MAP = Map.of(
		"name", BY_NAME,
		"levels", BY_LEVEL,
		"surfaces", (t1, t2) -> compareLists(mapList(t1.getSurfaces(), Surface::decode), mapList(t2.getSurfaces(), Surface::decode)),
		"speeds", (t1, t2) -> {
				var speeds1 = t1.getSpeeds().values().stream().sorted(reverseOrder()).collect(toList());
				var speeds2 = t2.getSpeeds().values().stream().sorted(reverseOrder()).collect(toList());
			return compareLists(speeds1, speeds2);
		},
		"eventCount", comparing(PlayerTournament::getEventCount),
		"bestResult", comparing(PlayerTournament::bestResultOrder),
		"lastResult", comparing(PlayerTournament::lastResultOrder),
		"wonPct", comparing(PlayerTournament::wonLost),
		"titles", comparing(PlayerTournament::getTitles)
	);

	@GetMapping("/playerTournamentsTable")
	public BootgridTable<PlayerTournament> playerTournamentsTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = new TournamentEventResultFilter(null, null, level, surface, indoor, speedRange, result, null, null, searchPhrase);
		var comparator = getComparator(requestParams, ORDER_MAP, BY_LEVEL.thenComparing(BY_NAME));
		var pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return sortAndPage(tournamentService.getPlayerTournaments(playerId, filter), comparator, pageSize, current);
	}
}
