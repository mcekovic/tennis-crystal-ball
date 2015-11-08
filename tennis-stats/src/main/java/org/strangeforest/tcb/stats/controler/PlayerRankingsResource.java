package org.strangeforest.tcb.stats.controler;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@RestController
public class PlayerRankingsResource {

	@Autowired private RankingsService rankingsService;

	private static final String CAREER = "CR";
	private static final String CUSTOM = "CS";

	@RequestMapping("/playerRankingsTable")
	public DataTable playerRankingsTable(
		@RequestParam(value = "players") String playersCSV,
		@RequestParam(value = "timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate fromDate,
		@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate toDate,
		@RequestParam(value = "rankType", defaultValue = "RANK") RankType rankType,
		@RequestParam(value = "compensatePoints", defaultValue = "false") boolean compensatePoints
	) {
		List<String> inputPlayers = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
		Range<LocalDate> dateRange = toDateRange(timeSpan, fromDate, toDate);
		return rankingsService.getRankingsDataTable(inputPlayers, dateRange, rankType, compensatePoints);
	}

	private Range<LocalDate> toDateRange(String timeSpan, LocalDate fromDate, LocalDate toDate) {
		switch (timeSpan) {
			case CAREER:
				return Range.all();
			case CUSTOM:
				if (fromDate != null)
					return toDate != null ? Range.closed(fromDate, toDate) : Range.atLeast(fromDate);
				else
					return toDate != null ? Range.atMost(toDate) : Range.all();
			default:
				return Range.atLeast(LocalDate.now().minusYears(Long.parseLong(timeSpan)));
		}
	}
}
