package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@RestController
public class StatsChartResource {

	@Autowired private StatisticsChartService statsChartService;

	@RequestMapping("/playerStatisticsTable")
	public DataTable playerStatisticsTable(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "players", required = false) String playersCSV,
		@RequestParam(value = "category", defaultValue = "matchesWonPct") String category,
		@RequestParam(value = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(value = "toSeason", required = false) Integer toSeason,
		@RequestParam(value = "byAge", defaultValue = "false") boolean byAge
	) {
		StatsCategory statsCategory = StatsCategory.get(category);
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		if (playerId != null)
			return statsChartService.getStatisticsDataTable(playerId, statsCategory, seasonRange, byAge);
		else {
			List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
			return statsChartService.getStatisticsDataTable(players, statsCategory, seasonRange, byAge);
		}
	}
}
