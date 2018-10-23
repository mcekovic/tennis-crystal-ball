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

import static org.strangeforest.tcb.util.DateUtil.*;

@RestController
public class PlayerEventsResource {

	@Autowired private TournamentService tournamentService;
	@Autowired private StatisticsService statisticsService;

	private static final int MAX_TOURNAMENTS = 1000;

	private static Map<String, String> ORDER_MAP = ImmutableMap.<String, String>builder()
		.put("season", "season")
		.put("date", "date")
		.put("name", "name")
		.put("surface", "surface")
		.put("speed", "court_speed NULLS LAST")
		.put("draw", "draw_type, draw_size")
		.put("participation", "participation")
		.put("strength", "strength")
		.put("averageEloRating", "average_elo_rating")
		.put("result", "result")
	.build();
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("date");

	@GetMapping("/playerEventsTable")
	public BootgridTable<PlayerTournamentEvent> playerEventsTable(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "current", defaultValue = "1") int current,
		@RequestParam(name = "rowCount", defaultValue = "20") int rowCount,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		StatsFilter statsFilter = StatsFilter.forTournaments(statsCategory, statsFrom, statsTo);
		TournamentEventResultFilter filter = new TournamentEventResultFilter(season, dateRange, level, surface, indoor, speedRange, result, tournamentId, statsFilter, searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		return tournamentService.getPlayerTournamentEventsTable(playerId, filter, orderBy, pageSize, current);
	}

	@GetMapping("/playerEventsStat")
	public Number playerEventsStat(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "searchPhrase", defaultValue="") String searchPhrase
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		MatchFilter filter = MatchFilter.forStats(season, dateRange, level, surface, indoor, speedRange, result, tournamentId, StatsFilter.ALL, searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);
		return StatsCategory.get(statsCategory).getStat(stats);
	}
}
