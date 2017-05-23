package org.strangeforest.tcb.stats.controller;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@RestController
public class PerfStatsChartResource {

	@Autowired private PerformanceChartService perfChartService;
	@Autowired private StatisticsChartService statsChartService;

	@GetMapping("/playerPerformanceTable")
	public DataTable playerPerformanceTable(
		@RequestParam(name = "playerId", required = false) int[] playerId,
		@RequestParam(name = "players", defaultValue = "") String playersCSV,
		@RequestParam(name = "category", defaultValue = "matches") String category,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = "false") boolean byAge
	) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		if (!seasonRange.equals(Range.all()))
			byAge = false;
		if (playerId != null && playerId.length > 0)
			return perfChartService.getPerformanceDataTable(playerId, perfCategory, seasonRange, byAge);
		else {
			List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
			return perfChartService.getPerformanceDataTable(players, perfCategory, seasonRange, byAge);
		}
	}

	@GetMapping("/playerStatisticsTable")
	public DataTable playerStatisticsTable(
		@RequestParam(name = "playerId", required = false) int[] playerId,
		@RequestParam(name = "players", defaultValue = "") String playersCSV,
		@RequestParam(name = "category", defaultValue = "aces") String category,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = "false") boolean byAge
	) {
		StatsCategory statsCategory = StatsCategory.get(category);
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		if (!seasonRange.equals(Range.all()))
			byAge = false;
		if (playerId != null && playerId.length > 0)
			return statsChartService.getStatisticsDataTable(playerId, statsCategory, surface, seasonRange, byAge);
		else {
			List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
			return statsChartService.getStatisticsDataTable(players, statsCategory, surface, seasonRange, byAge);
		}
	}
}
