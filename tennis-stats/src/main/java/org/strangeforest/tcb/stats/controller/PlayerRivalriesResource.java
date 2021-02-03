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
public class PlayerRivalriesResource {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private MatchesService matchesService;

	private static final int MAX_RIVALRIES = 1000;

	private static final Map<String, String> ORDER_MAP = Map.of(
		"bestRank", "best_rank",
		"matches", "matches",
		"won", "won",
		"lost", "lost",
		"wonPctStr", "CASE WHEN won + lost > 0 THEN won::REAL / (won + lost) ELSE 0 END"
	);
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("matches"), asc("best_rank"), desc("won")};

	@GetMapping("/playerRivalriesTable")
	public BootgridTable<PlayerRivalryRow> playerRivalriesTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "h2h", required = false) Integer h2h,
		@RequestParam(name = "matches", required = false) Integer matches,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		var seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var rivalryFilter = new RivalryFilter(season, seasonRange, level, bestOf, surface, indoor, speedRange, round, tournamentId);
		var rivalrySeriesFilter = new RivalrySeriesFilter(opponent, matchesService.getSameCountryIds(countryId), h2h, matches);
		var filter = new RivalryPlayerListFilter(searchPhrase, rivalryFilter);
		var orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		var pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getPlayerRivalriesTable(playerId, filter, rivalrySeriesFilter, orderBy, pageSize, current);
	}

	@GetMapping("/h2h")
	public List<Integer> h2h(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "outcome", required = false) String outcome
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, season, dateRange, level, bestOf, surface, indoor, speedRange, round, tournamentId, outcome, score));
		return List.of(stats1.getMatchesWon(), stats1.getMatchesLost());
	}
}
