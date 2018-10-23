package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@RestController
public class TopPerformersResource {

	@Autowired private TopPerformersService topPerformersService;
	@Autowired private MatchesService matchesService;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("wonLostPct", "won_lost_pct")
		.put("won", "won")
		.put("lost", "lost")
		.put("played", "played")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("won"), asc("lost"), asc("name")};

	@GetMapping("/topPerformersTable")
	public BootgridTable<TopPerformerRow> topPerformersTable(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minEntries", required = false) Integer minEntries,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PerfStatsFilter filter = new PerfStatsFilter(active, searchPhrase, season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, null, opponentFilter);
		TopPerformersView view = new TopPerformersView(category, filter).optimize();
		int playerCount = topPerformersService.getPlayerCount(view.getCategory(), view.getFilter(), minEntries);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return topPerformersService.getTopPerformersTable(view.getCategory(), playerCount, view.getFilter(), minEntries, orderBy, pageSize, current);
	}

	@GetMapping("/topPerformersMinEntries")
	public String topPerformersMinEntries(
		@RequestParam(name = "category") String category,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minEntries", required = false) Integer minEntries
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PerfStatsFilter filter = new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter);
		TopPerformersView view = new TopPerformersView(category, filter).optimize();
		return topPerformersService.getTopPerformersMinEntries(view.getCategory(), view.getFilter(), minEntries);
	}
}
