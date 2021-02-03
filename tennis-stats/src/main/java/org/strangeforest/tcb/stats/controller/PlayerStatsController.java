package org.strangeforest.tcb.stats.controller;

import java.time.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class PlayerStatsController extends BaseController {

	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private MatchesService matchesService;

	@GetMapping("/tournamentsStats")
	public ModelAndView tournamentsStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = MatchFilter.forStats(null, null, level, surface, indoor, speedRange, result, null, null, searchPhrase);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("tournamentsStats", modelMap);
	}

	@GetMapping("/eventsStats")
	public ModelAndView eventsStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var statsFilter = StatsFilter.forStats(statsCategory, statsFrom, statsTo);
		var filter = MatchFilter.forStats(season, dateRange, level, surface, indoor, speedRange, result, tournamentId, statsFilter, searchPhrase);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("eventsStats", modelMap);
	}

	@GetMapping("/matchesStats")
	public ModelAndView matchesStats(
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
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "bigWin", defaultValue = "false") boolean bigWin,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface,
		@RequestParam(name = "playerIndex", required = false) Integer playerIndex
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		var outcomeFilter = OutcomeFilter.forStats(outcome);
		var statsFilter = StatsFilter.forStats(statsCategory, statsFrom, statsTo);
		var scoreFilter = ScoreFilter.forStats(score);
		var filter = MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, statsFilter, bigWin, searchPhrase);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		modelMap.addAttribute("playerIndex", playerIndex);
		return new ModelAndView("matchesStats", modelMap);
	}

	@GetMapping("/tournamentPlayerStats")
	public ModelAndView tournamentPlayerStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentId") int tournamentId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = MatchFilter.forStats(null, null, level, surface, indoor, speedRange, result, tournamentId, null, searchPhrase);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("tournamentPlayerStats", modelMap);
	}

	@GetMapping("/eventStats")
	public ModelAndView eventStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentEventId") int tournamentEventId,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var filter = MatchFilter.forTournamentEvent(tournamentEventId);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("eventStats", modelMap);
	}

	@GetMapping("/seedStats")
	public ModelAndView seedStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentEventId") int tournamentEventId,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var filter = MatchFilter.forTournamentEvent(tournamentEventId);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("seedStats", modelMap);
	}

	@GetMapping("/rivalryStats")
	public ModelAndView rivalryStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "opponentId") int opponentId,
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
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = MatchFilter.forOpponent(opponentId, season, dateRange, level, bestOf, surface, indoor, speedRange, round, tournamentId, outcome, score);
		var stats = statisticsService.getPlayerStats(playerId, filter);

		var modelMap = new ModelMap();
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("rivalryStats", modelMap);
	}

	@GetMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(name = "matchId") long matchId,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", defaultValue = F) boolean compareSeason,
		@RequestParam(name = "compareLevel", defaultValue = F) boolean compareLevel,
		@RequestParam(name = "compareSurface", defaultValue = F) boolean compareSurface,
		@RequestParam(name = "compareRound", defaultValue = F) boolean compareRound,
		@RequestParam(name = "compareOpponent", defaultValue = F) boolean compareOpponent,
		@RequestParam(name = "inProgress", defaultValue = F) boolean inProgress
	) {
		var matchStats = statisticsService.getMatchStats(matchId, inProgress);

		var modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("categoryGroups", StatsCategory.getMatchCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("matchStats", matchStats);
		addCompareMatchStats(modelMap, matchId, compare, compareSeason, compareLevel, compareSurface, compareRound, compareOpponent, inProgress);
		return new ModelAndView("matchStats", modelMap);
	}


	// Compare statistics util

	private void addCompareStats(ModelMap modelMap, int playerId, boolean compare, Integer compareSeason, String compareLevel, String compareSurface) {
		modelMap.addAttribute("compare", compare);
		if (compare) {
			var compareFilter = MatchFilter.forStats(compareSeason, compareLevel, compareSurface);
			var compareStats = statisticsService.getPlayerStats(playerId, compareFilter);
			var relativeTo = relativeTo(compareSeason, compareLevel, compareSurface);

			if (!compareStats.isEmpty())
				modelMap.addAttribute("compareStats", compareStats);
			modelMap.addAttribute("seasons", playerService.getPlayerSeasons(playerId));
			modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
			modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
			modelMap.addAttribute("surfaces", Surface.values());
			modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
			modelMap.addAttribute("compareSeason", compareSeason);
			modelMap.addAttribute("compareLevel", compareLevel);
			modelMap.addAttribute("compareSurface", compareSurface);
			modelMap.addAttribute("relativeTo", relativeTo);
		}
	}

	private void addCompareMatchStats(ModelMap modelMap, long matchId, boolean compare, boolean compareSeason, boolean compareLevel, boolean compareSurface, boolean compareRound, boolean compareOpponent, boolean inProgress) {
		modelMap.addAttribute("compare", compare);
		if (compare) {
			var match = matchesService.getMatch(matchId, inProgress);
			var season = compareSeason ? match.getSeason() : null;
			var level = compareLevel ? match.getLevel() : null;
			var surface = compareSurface ? match.getSurface() : null;
			var round = compareRound ? match.getRound() : null;
			var playerId1 = match.getWinnerId();
			var playerId2 = match.getLoserId();
			var opponentId1 = compareOpponent ? playerId2 : null;
			var opponentId2 = compareOpponent ? playerId1 : null;
			var compareFilter1 = MatchFilter.forStats(season, level, surface, round, opponentId1);
			var compareStats1 = statisticsService.getPlayerStats(playerId1, compareFilter1);
			var relativeTo1 = relativeTo(season, level, surface, round, opponentId1 != null ? playerService.getPlayerName(opponentId1) : null);
			var compareFilter2 = MatchFilter.forStats(season, level, surface, round, opponentId2);
			var compareStats2 = statisticsService.getPlayerStats(playerId2, compareFilter2);
			var relativeTo2 = relativeTo(season, level, surface, round, opponentId2 != null ? playerService.getPlayerName(opponentId2) : null);

			if (!compareStats1.isEmpty())
				modelMap.addAttribute("compareStats1", compareStats1);
			if (!compareStats2.isEmpty())
				modelMap.addAttribute("compareStats2", compareStats2);
			modelMap.addAttribute("match", match);
			modelMap.addAttribute("matchLevel", TournamentLevel.decode(match.getLevel()).getText());
			if (match.getSurface() != null)
				modelMap.addAttribute("matchSurface", Surface.decode(match.getSurface()).getText());
			modelMap.addAttribute("matchRound", Round.decode(match.getRound()).getText());
			modelMap.addAttribute("compareSeason", compareSeason);
			modelMap.addAttribute("compareLevel", compareLevel);
			modelMap.addAttribute("compareSurface", compareSurface);
			modelMap.addAttribute("compareRound", compareRound);
			modelMap.addAttribute("compareOpponent", compareOpponent);
			modelMap.addAttribute("relativeTo1", relativeTo1);
			modelMap.addAttribute("relativeTo2", relativeTo2);
		}
	}
}
