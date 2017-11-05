package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class PlayerStatsController extends BaseController {

	@Autowired private PlayerService playerService;
	@Autowired private PerformanceService performanceService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerTimelineService timelineService;
	@Autowired private MatchesService matchesService;

	@GetMapping("/eventsStats")
	public ModelAndView eventsStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "statsCategory", required = false) String statsCategory,
		@RequestParam(name = "statsFrom", required = false) Double statsFrom,
		@RequestParam(name = "statsTo", required = false) Double statsTo,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		StatsFilter statsFilter = new StatsFilter(statsCategory, statsFrom, statsTo);
		MatchFilter filter = MatchFilter.forStats(season, dateRange, level, surface, indoor, result, tournamentId, statsFilter, searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("stats", stats);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
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
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		OutcomeFilter outcomeFilter = OutcomeFilter.forStats(outcome);
		StatsFilter statsFilter = new StatsFilter(statsCategory, statsFrom, statsTo);
		ScoreFilter scoreFilter = ScoreFilter.forStats(score);
		MatchFilter filter = MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, statsFilter, searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("stats", stats);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("matchesStats", modelMap);
	}

	@GetMapping("/eventStats")
	public ModelAndView eventStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentEventId") int tournamentEventId,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		MatchFilter filter = MatchFilter.forTournamentEvent(tournamentEventId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("stats", stats);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("eventStats", modelMap);
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
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		MatchFilter filter = MatchFilter.forOpponent(opponentId, season, dateRange, level, bestOf, surface, indoor, round, tournamentId, outcome, score);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("stats", stats);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("rivalryStats", modelMap);
	}

	@GetMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(name = "matchId") long matchId,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", defaultValue = F) boolean compareSeason,
		@RequestParam(name = "compareLevel", defaultValue = F) boolean compareLevel,
		@RequestParam(name = "compareSurface", defaultValue = F) boolean compareSurface,
		@RequestParam(name = "compareRound", defaultValue = F) boolean compareRound,
		@RequestParam(name = "compareOpponent", defaultValue = F) boolean compareOpponent
	) {
		MatchStats matchStats = statisticsService.getMatchStats(matchId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("matchStats", matchStats);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
		addCompareMatchStats(modelMap, matchId, compare, compareSeason, compareLevel, compareSurface, compareRound, compareOpponent);
		return new ModelAndView("matchStats", modelMap);
	}

	@GetMapping("/playerTimelineStats")
	public ModelAndView playerTimelineStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		PlayerStats careerStats = statisticsService.getPlayerStats(playerId);
		List<Integer> seasonList = toSeasons(seasons);
		Map<Integer, PlayerStats> seasonsStats = statisticsService.getPlayerSeasonsStats(playerId);
		ensureSeasons(seasonsStats, seasonList, PlayerStats.EMPTY);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("careerStats", careerStats);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsStats", seasonsStats);
		return new ModelAndView("playerTimelineStats", modelMap);
	}

	@GetMapping("/playerTimelinePerformance")
	public ModelAndView playerTimelinePerformance(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		Map<Integer, Integer> titles = timelineService.getPlayerSeasonTitles(playerId);
		Map<Integer, Integer> yearEndRanks = timelineService.getPlayerYearEndRanks(playerId);
		Map<Integer, Integer> bestEloRatings = timelineService.getPlayerBestEloRatings(playerId);
		Map<Integer, Integer> goatPoints = timelineService.getPlayerSeasonGOATPoints(playerId);
		PlayerPerformance careerPerf = performanceService.getPlayerPerformance(playerId);
		List<Integer> seasonList = toSeasons(seasons);
		Map<Integer, PlayerPerformance> seasonsPerf = performanceService.getPlayerSeasonsPerformance(playerId);
		ensureSeasons(seasonsPerf, seasonList, PlayerPerformance.EMPTY);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("titles", titles);
		modelMap.addAttribute("yearEndRanks", yearEndRanks);
		modelMap.addAttribute("bestEloRatings", bestEloRatings);
		modelMap.addAttribute("goatPoints", goatPoints);
		modelMap.addAttribute("careerPerf", careerPerf);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsPerf", seasonsPerf);
		return new ModelAndView("playerTimelinePerformance", modelMap);
	}

	private List<Integer> toSeasons(String seasons) {
		return Stream.of(seasons.split(",")).map(Integer::valueOf).collect(toList());
	}

	private <T> void ensureSeasons(Map<Integer, T> seasonsData, List<Integer> seasons, T empty) {
		for (Integer season : seasons) {
			if (!seasonsData.containsKey(season))
				seasonsData.put(season, empty);
		}
	}


	// Compare statistics util

	private void addCompareStats(ModelMap modelMap, int playerId, boolean compare, Integer compareSeason, String compareLevel, String compareSurface) {
		modelMap.addAttribute("compare", compare);
		if (compare) {
			MatchFilter compareFilter = MatchFilter.forStats(compareSeason, compareLevel, compareSurface);
			PlayerStats compareStats = statisticsService.getPlayerStats(playerId, compareFilter);
			String relativeTo = relativeTo(compareSeason, compareLevel, compareSurface);

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

	private void addCompareMatchStats(ModelMap modelMap, long matchId, boolean compare, boolean compareSeason, boolean compareLevel, boolean compareSurface, boolean compareRound, boolean compareOpponent) {
		modelMap.addAttribute("compare", compare);
		if (compare) {
			MatchInfo match = matchesService.getMatch(matchId);
			Integer season = compareSeason ? match.getSeason() : null;
			String level = compareLevel ? match.getLevel() : null;
			String surface = compareSurface ? match.getSurface() : null;
			String round = compareRound ? match.getRound() : null;
			Integer winnerOpponentId = compareOpponent ? match.getLoserId() : null;
			Integer loserOpponentId = compareOpponent ? match.getWinnerId() : null;
			MatchFilter winnerCompareFilter = MatchFilter.forStats(season, level, surface, round, winnerOpponentId);
			PlayerStats winnerCompareStats = statisticsService.getPlayerStats(match.getWinnerId(), winnerCompareFilter);
			String winnerRelativeTo = relativeTo(season, level, surface, round, winnerOpponentId != null ? playerService.getPlayerName(winnerOpponentId) : null);
			MatchFilter loserCompareFilter = MatchFilter.forStats(season, level, surface, round, loserOpponentId);
			PlayerStats loserCompareStats = statisticsService.getPlayerStats(match.getLoserId(), loserCompareFilter);
			String loserRelativeTo = relativeTo(season, level, surface, round, loserOpponentId != null ? playerService.getPlayerName(loserOpponentId) : null);

			if (!winnerCompareStats.isEmpty())
				modelMap.addAttribute("winnerCompareStats", winnerCompareStats);
			if (!loserCompareStats.isEmpty())
				modelMap.addAttribute("loserCompareStats", loserCompareStats);
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
			modelMap.addAttribute("winnerRelativeTo", winnerRelativeTo);
			modelMap.addAttribute("loserRelativeTo", loserRelativeTo);
		}
	}
}
