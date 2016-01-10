package org.strangeforest.tcb.stats.controler;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@RestController
public class PlayerRankingsResource {

	@Autowired private RankingsService rankingsService;

	private static final String CAREER = "CR";
	private static final String CUSTOM = "CS";

	@RequestMapping("/playerRankingsTable")
	public DataTable playerRankingsTable(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "players", required = false) String playersCSV,
		@RequestParam(value = "rankType", defaultValue = "RANK") RankType rankType,
		@RequestParam(value = "timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(value = "bySeason", defaultValue = "false") boolean bySeason,
		@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate fromDate,
		@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate toDate,
		@RequestParam(value = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(value = "toSeason", required = false) Integer toSeason,
		@RequestParam(value = "byAge", defaultValue = "false") boolean byAge,
		@RequestParam(value = "compensatePoints", defaultValue = "false") boolean compensatePoints
	) {
		Range<LocalDate> dateRange = !bySeason ? toDateRange(timeSpan, fromDate, toDate) : null;
		Range<Integer> seasonRange = bySeason ? toSeasonRange(timeSpan, fromSeason, toSeason) : null;
		if (playerId != null)
			return rankingsService.getRankingDataTable(playerId, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		else {
			List<String> inputPlayers = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
			return rankingsService.getRankingsDataTable(inputPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		}
	}

	private Range<LocalDate> toDateRange(String timeSpan, LocalDate fromDate, LocalDate toDate) {
		switch (timeSpan) {
			case CAREER:
				return Range.all();
			case CUSTOM:
				return RangeUtil.toRange(fromDate, toDate);
			default:
				return Range.atLeast(LocalDate.now().minusYears(Long.parseLong(timeSpan)));
		}
	}

	private Range<Integer> toSeasonRange(String timeSpan, Integer fromSeason, Integer toSeason) {
		switch (timeSpan) {
			case CAREER:
				return Range.all();
			case CUSTOM:
				return RangeUtil.toRange(fromSeason, toSeason);
			default:
				return Range.atLeast(LocalDate.now().getYear() - Integer.parseInt(timeSpan));
		}
	}
}
