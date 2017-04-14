package org.strangeforest.tcb.stats.controller;

import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@RestController
public class RankingsResource {

	@Autowired private RankingsService rankingsService;
	@Autowired private RankingChartService rankingChartService;

	private static final String CAREER = "CR";
	private static final String CUSTOM = "CS";

	private static final int MAX_PLAYERS = 1000;

	@GetMapping("/rankingsTableTable")
	public BootgridTable<PlayerRankingsRow> rankingsTableTable(
		@RequestParam(name = "rankType") RankType rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date,
		@RequestParam(name = "peak", required = false) boolean peak,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase
	) {
		if (date == null) {
			if (season != null)
				date = rankingsService.getSeasonEndRankingDate(rankType, season);
			if (date == null && !peak)
				date = rankingsService.getCurrentRankingDate(rankType);
		}
		PlayerListFilter filter = new PlayerListFilter(active, searchPhrase);
		int pageSize = rowCount > 0 ? rowCount : MAX_PLAYERS;
		return rankingsService.getRankingsTable(rankType, date, filter, pageSize, current);
	}

	@GetMapping("/seasonRankingDates")
	public List<Date> seasonRankingDates(
		@RequestParam(name = "rankType") RankType rankType,
		@RequestParam(name = "season") int season
	) {
		return rankingsService.getSeasonRankingDates(rankType, season);
	}

	@GetMapping("/playerRankingsTable")
	public DataTable playerRankingsTable(
		@RequestParam(name = "playerId", required = false) int[] playerId,
		@RequestParam(name = "players", required = false) String playersCSV,
		@RequestParam(name = "rankType", defaultValue = "RANK") RankType rankType,
		@RequestParam(name = "timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(name = "bySeason", defaultValue = "false") boolean bySeason,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate toDate,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = "false") boolean byAge,
		@RequestParam(name = "compensatePoints", defaultValue = "false") boolean compensatePoints
	) {
		Range<LocalDate> dateRange = !bySeason ? toDateRange(timeSpan, fromDate, toDate) : null;
		Range<Integer> seasonRange = bySeason ? toSeasonRange(timeSpan, fromSeason, toSeason) : null;
		if (playerId != null && playerId.length > 0)
			return rankingChartService.getRankingDataTable(playerId, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		else {
			List<String> inputPlayers = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
			return rankingChartService.getRankingsDataTable(inputPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
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
