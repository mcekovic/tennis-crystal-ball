package org.strangeforest.tcb.stats.controller;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

@Controller
public class PlayerStatsController extends BaseController {

	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerTimelineService timelineService;

	@GetMapping("/eventsStats")
	public ModelAndView eventsStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase
	) {
		MatchFilter filter = MatchFilter.forStats(season, level, surface, tournamentId, null, result, null, null, null, searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		return new ModelAndView("eventsStats", "stats", stats);
	}

	@GetMapping("/matchesStats")
	public ModelAndView matchesStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase
	) {
		MatchFilter filter = MatchFilter.forStats(season, level, surface, tournamentId, tournamentEventId, null, round, OpponentFilter.forStats(opponent), OutcomeFilter.forStats(outcome), searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		return new ModelAndView("matchesStats", "stats", stats);
	}

	@GetMapping("/eventStats")
	public ModelAndView eventStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		MatchFilter filter = MatchFilter.forTournamentEvent(tournamentEventId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("eventStats", modelMap);
	}

	@GetMapping("/rivalryStats")
	public ModelAndView rivalryStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "opponentId") int opponentId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round
	) {
		MatchFilter filter = MatchFilter.forOpponent(opponentId, level, surface, round);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("rivalryStats", modelMap);
	}

	@GetMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(name = "matchId") long matchId
	) {
		MatchStats matchStats = statisticsService.getMatchStats(matchId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("matchStats", matchStats);
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
		Map<Integer, Integer> yearEndEloRatings = timelineService.getPlayerYearEndEloRatings(playerId);
		Map<Integer, Integer> goatPoints = timelineService.getPlayerSeasonGOATPoints(playerId);
		PlayerPerformance careerPerf = statisticsService.getPlayerPerformance(playerId);
		List<Integer> seasonList = toSeasons(seasons);
		Map<Integer, PlayerPerformance> seasonsPerf = statisticsService.getPlayerSeasonsPerformance(playerId);
		ensureSeasons(seasonsPerf, seasonList, PlayerPerformance.EMPTY);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("titles", titles);
		modelMap.addAttribute("yearEndRanks", yearEndRanks);
		modelMap.addAttribute("yearEndEloRatings", yearEndEloRatings);
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
}
