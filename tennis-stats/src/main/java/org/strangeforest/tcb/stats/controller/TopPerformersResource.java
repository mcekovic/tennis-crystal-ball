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

import static org.strangeforest.tcb.stats.service.TopPerformersService.*;
import static org.strangeforest.tcb.stats.util.OrderBy.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@RestController
public class TopPerformersResource {

	@Autowired private TopPerformersService topPerformersService;
	@Autowired private MatchesService matchesService;


	// Top Performers

	private static final Map<String, String> ORDER_MAP = Map.of(
		"wonLostPct", "won_lost_pct",
		"won", "won",
		"lost", "lost",
		"played", "played"
	);
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
		@RequestParam(name = "speed", required = false) String speed,
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
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var filter = new PerfStatsFilter(active, searchPhrase, season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, null, opponentFilter, null);
		var view = new TopPerformersView(category, filter).optimize();
		var playerCount = topPerformersService.getTopPerformersPlayerCount(view.getCategory(), view.getFilter(), minEntries);

		var orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		var pageSize = rowCount > 0 ? rowCount : playerCount;
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
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minEntries", required = false) Integer minEntries
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var filter = new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter);
		var view = new TopPerformersView(category, filter).optimize();
		return topPerformersService.getTopPerformersMinEntries(view.getCategory(), view.getFilter(), minEntries);
	}


	// Titles and Results

	private static final Map<String, String> TITLES_ORDER_MAP = Map.of("count", "count");
	private static final OrderBy[] TITLES_DEFAULT_ORDERS = new OrderBy[] {desc("count"), asc("last_date")};

	@GetMapping("/titlesAndResultsTable")
	public BootgridTable<PlayerTitlesRow> titlesAndResultsTable(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = new PerfStatsFilter(active, searchPhrase, season, dateRange, level, null, surface, indoor, speedRange, null, result, tournamentId, null, null, null);
		var playerCount = topPerformersService.getTitlesAndResultsPlayerCount(filter);

		var orderBy = BootgridUtil.getOrderBy(requestParams, TITLES_ORDER_MAP, TITLES_DEFAULT_ORDERS);
		var pageSize = rowCount > 0 ? rowCount : playerCount;
		return topPerformersService.getTitlesAndResultsTable(playerCount, filter, orderBy, pageSize, current);
	}


	// Mental Toughness

	private static final Map<String, String> MENTAL_TOUGHNESS_ORDER_MAP = Map.of(
		"rating", "rating",
		"pointsWon", "points_won",
		"pointsLost", "points_lost",
		"decidingSetsPct", "deciding_sets_won / nullif(deciding_sets_won + deciding_sets_lost, 0) NULLS LAST",
		"fifthSetsPct", "fifth_sets_won / nullif(fifth_sets_won + fifth_sets_lost, 0) NULLS LAST",
		"finalsPct", "finals_won / nullif(finals_won + finals_lost, 0) NULLS LAST",
		"tieBreaksPct", "tie_breaks_won / nullif(tie_breaks_won + tie_breaks_lost, 0) NULLS LAST",
		"decidingSetTieBreaksPct", "deciding_set_tbs_won / nullif(deciding_set_tbs_won + deciding_set_tbs_lost, 0) NULLS LAST"
	);
	private static final OrderBy[] MENTAL_TOUGHNESS_DEFAULT_ORDERS = new OrderBy[] {desc("rating"), desc("points_won")};

	@GetMapping("/mentalToughnessTable")
	public BootgridTable<MentalToughnessRow> mentalToughnessTable(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "minPoints", required = false) Integer minPoints,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var filter = new PerfStatsFilter(active, searchPhrase, season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, null, opponentFilter, null);
		var playerCount = topPerformersService.getMentalToughnessPlayerCount(filter, minPoints);

		var orderBy = BootgridUtil.getOrderBy(requestParams, MENTAL_TOUGHNESS_ORDER_MAP, MENTAL_TOUGHNESS_DEFAULT_ORDERS);
		var pageSize = rowCount > 0 ? rowCount : playerCount;
		return topPerformersService.getMentalToughnessTable(playerCount, filter, minPoints, orderBy, pageSize, current);
	}

	@GetMapping("/mentalToughnessMinPoints")
	public String mentalToughnessMinPoints(
			@RequestParam(name = "season", required = false) Integer season,
			@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
			@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
			@RequestParam(name = "level", required = false) String level,
			@RequestParam(name = "bestOf", required = false) Integer bestOf,
			@RequestParam(name = "surface", required = false) String surface,
			@RequestParam(name = "indoor", required = false) Boolean indoor,
			@RequestParam(name = "speed", required = false) String speed,
			@RequestParam(name = "round", required = false) String round,
			@RequestParam(name = "result", required = false) String result,
			@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
			@RequestParam(name = "opponent", required = false) String opponent,
			@RequestParam(name = "countryId", required = false) String countryId,
			@RequestParam(name = "minPoints", required = false) Integer minPoints
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var filter = new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter);
		return topPerformersService.getMentalToughnessMinPoints(filter, minPoints);
	}
}
