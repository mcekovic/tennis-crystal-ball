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

import static java.util.Comparator.*;
import static org.strangeforest.tcb.stats.util.BootgridUtil.*;
import static org.strangeforest.tcb.util.CompareUtil.*;

@RestController
public class TournamentsResource {

	@Autowired private TournamentService tournamentService;

	private static final int MAX_TOURNAMENTS = 1000;

	private static final Comparator<Tournament> BY_NAME = nullsLast(comparing(Tournament::getName));
	private static final Comparator<Tournament> BY_LEVEL = (t1, t2) -> compareLists(mapSortList(t1.getLevels(), TournamentLevel::decode), mapSortList(t2.getLevels(), TournamentLevel::decode));
	private static Map<String, Comparator<Tournament>> ORDER_MAP = ImmutableMap.<String, Comparator<Tournament>>builder()
		.put("name", BY_NAME)
		.put("levels", BY_LEVEL)
		.put("surfaces", (t1, t2) -> compareLists(mapList(t1.getSurfaces(), Surface::decode), mapList(t2.getSurfaces(), Surface::decode)))
		.put("speeds", (t1, t2) -> {
			List<Integer> speeds1 = new ArrayList<>(t1.getSpeeds().values());
			speeds1.sort(reverseOrder());
			List<Integer> speeds2 = new ArrayList<>(t2.getSpeeds().values());
			speeds2.sort(reverseOrder());
			return compareLists(speeds1, speeds2);
		})
		.put("eventCount", comparing(Tournament::getEventCount))
		.put("playerCount", comparing(Tournament::getPlayerCount))
		.put("participation", comparing(Tournament::getParticipation))
		.put("strength", comparing(Tournament::getStrength))
		.put("averageEloRating", comparing(Tournament::getAverageEloRating))
	.build();

	@GetMapping("/tournamentsTable")
	public BootgridTable<Tournament> tournamentsTable(
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		TournamentEventFilter filter = new TournamentEventFilter(null, null, level, surface, indoor, speedRange, null, null, searchPhrase);
		Comparator<Tournament> comparator = BootgridUtil.getComparator(requestParams, ORDER_MAP, BY_LEVEL.thenComparing(BY_NAME));
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return sortAndPage(tournamentService.getTournaments(filter), comparator, pageSize, current);
	}
}
