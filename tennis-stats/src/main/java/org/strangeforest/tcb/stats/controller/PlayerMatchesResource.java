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
public class PlayerMatchesResource {

	@Autowired private MatchesService matchesService;
	@Autowired private StatisticsService statisticsService;

	private static final int MAX_MATCHES = 10000;

	private static final Map<String, String> ORDER_MAP = Map.of(
		"date", "date",
		"tournament", "tournament",
		"surface", "surface",
		"speed", "court_speed NULLS LAST",
		"round", "round",
		"wonLost", "CASE WHEN outcome = 'ABD' THEN 0 WHEN pw.player_id = :playerId THEN 1 ELSE -1 END",
		"bestOf", "best_of"
	);
	private static final Map<String, String> ORDER_MAP_BIG_WINS = ImmutableMap.<String, String>builder()
		.putAll(ORDER_MAP)
		.put("bigWinPoints", "big_win_points")
	.build();
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("date"), desc("round"), desc("match_num")};

	@GetMapping("/matchesTable")
	public BootgridTable<Match> matchesTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "bigWin", defaultValue = "false") boolean bigWin,
		@RequestParam(name = "h2h", defaultValue = "false") boolean h2h,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forMatches(opponent, matchesService.getSameCountryIds(countryId));
		var outcomeFilter = OutcomeFilter.forMatches(outcome);
		var scoreFilter = ScoreFilter.forMatches(score);
		var statsFilter = StatsFilter.forMatches(statsCategory, statsFrom, statsTo);
		var filter = MatchFilter.forMatches(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, statsFilter, bigWin, searchPhrase);
		var orderBy = BootgridUtil.getOrderBy(requestParams, bigWin ? ORDER_MAP_BIG_WINS : ORDER_MAP, DEFAULT_ORDERS);
		var pageSize = rowCount > 0 ? rowCount : MAX_MATCHES;
		return matchesService.getPlayerMatchesTable(playerId, filter, h2h, orderBy, pageSize, current);
	}
	
	@GetMapping("/matchesStat")
	public Number matchesStat(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "bigWin", defaultValue = "false") boolean bigWin,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var outcomeFilter = OutcomeFilter.forStats(outcome);
		var scoreFilter = ScoreFilter.forStats(score);
		var filter = MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, StatsFilter.ALL, bigWin, searchPhrase);
		var stats = statisticsService.getPlayerStats(playerId, filter);
		return StatsCategory.get(statsCategory).getStat(stats);
	}
}
