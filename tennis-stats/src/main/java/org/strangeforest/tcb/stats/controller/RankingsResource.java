package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;
import java.util.stream.*;

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

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.model.core.RankCategory.*;
import static org.strangeforest.tcb.stats.util.OrderBy.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@RestController
public class RankingsResource {

	@Autowired private RankingsService rankingsService;
	@Autowired private RankingChartService rankingChartService;

	static final String CAREER = "CR";
	static final String CUSTOM = "CS";

	private static final int MAX_PLAYERS = 1000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("rank", "rank")
		.put("bestRank", "best_rank")
		.put("points", "points NULLS LAST")
		.put("rankDiff", "rank_diff NULLS LAST")
		.put("pointsDiff", "points_diff NULLS LAST")
		.build();
	private static final OrderBy DEFAULT_ORDER = asc("rank");

	@GetMapping("/rankingsDate")
	public LocalDate rankingsDate(
		@RequestParam(name = "rankType") RankType rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date
	) {
		return rankingsService.getRankingsDate(rankType, season, date);
	}

	@GetMapping("/rankingsTableTable")
	public BootgridTable<? extends PlayerRankingsRow> rankingsTableTable(
		@RequestParam(name = "rankType") RankType rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
		@RequestParam(name = "peak", required = false) boolean peak,
		@RequestParam(name = "active", required = false) Boolean active,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(active, searchPhrase);
		int pageSize = rowCount > 0 ? rowCount : MAX_PLAYERS;
		if (rankType.category == ELO && peak)
			return rankingsService.getPeakEloRatingsTable(rankType, filter, pageSize, current, MAX_PLAYERS);
		else {
			date = rankingsService.getRankingsDate(rankType, season, date);
			String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
			return rankingsService.getRankingsTable(rankType, date, filter, orderBy, pageSize, current);
		}
	}

	@GetMapping("/seasonRankingDates")
	public List<LocalDate> seasonRankingDates(
		@RequestParam(name = "rankType") RankType rankType,
		@RequestParam(name = "season") int season
	) {
		return rankingsService.getSeasonRankingDates(rankType, season);
	}

	@GetMapping("/playerRankingsTable")
	public DataTable playerRankingsTable(
		@RequestParam(name = "playerId", required = false) int[] playerId,
		@RequestParam(name = "players", defaultValue = "") String playersCSV,
		@RequestParam(name = "rankType", defaultValue = "RANK") RankType rankType,
		@RequestParam(name = "timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(name = "bySeason", defaultValue = F) boolean bySeason,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = F) boolean byAge,
		@RequestParam(name = "compensatePoints", defaultValue = F) boolean compensatePoints
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
