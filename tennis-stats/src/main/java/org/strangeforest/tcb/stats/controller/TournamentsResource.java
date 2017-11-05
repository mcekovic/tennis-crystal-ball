package org.strangeforest.tcb.stats.controller;

import java.util.*;
import java.util.function.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
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
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		TournamentEventFilter filter = new TournamentEventFilter(null, null, level, surface, null, null, null, searchPhrase);
		Comparator<Tournament> comparator = BootgridUtil.getComparator(requestParams, ORDER_MAP, BY_LEVEL.thenComparing(BY_NAME));
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return tournamentService.getTournamentsTable(filter, comparator, pageSize, current);
	}

	private static <T, R> List<R> mapList(List<T> items, Function<? super T, ? extends R> mapper) {
		return items.stream().map(mapper).collect(toList());
	}

	private static <T, R extends Comparable> List<R> mapSortList(List<T> items, Function<? super T, ? extends R> mapper) {
		List<R> list = mapList(items, mapper);
		sort(list);
		return list;
	}
}
