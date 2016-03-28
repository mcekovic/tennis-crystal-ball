package org.strangeforest.tcb.stats.controler;

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
public class PlayerStatsController {

	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerTimelineService timelineService;

	@RequestMapping("/eventStats")
	public ModelAndView eventStats(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "tournamentEventId") int tournamentEventId
	) {
		MatchFilter filter = MatchFilter.forTournamentEvent(tournamentEventId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("eventStats", modelMap);
	}

	@RequestMapping("/rivalryStats")
	public ModelAndView rivalryStats(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "opponentId") int opponentId
	) {
		MatchFilter filter = MatchFilter.forOpponent(opponentId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("rivalryStats", modelMap);
	}

	@RequestMapping("/playerStats")
	public ModelAndView playerStats(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "round", required = false) String round,
		@RequestParam(value = "opponent", required = false) String opponent,
		@RequestParam(value = "outcome", required = false) String outcome,
		@RequestParam(value = "searchPhrase", required = false) String searchPhrase
	) {
		MatchFilter filter = MatchFilter.forStats(season, level, surface, tournamentId, tournamentEventId, round, OpponentFilter.forStats(opponent), OutcomeFilter.forStats(outcome), searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		return new ModelAndView("playerStats", "stats", stats);
	}

	@RequestMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(value = "matchId") long matchId
	) {
		MatchStats matchStats = statisticsService.getMatchStats(matchId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("matchStats", matchStats);
		return new ModelAndView("matchStats", modelMap);
	}

	@RequestMapping("/playerTimelineStats")
	public ModelAndView playerTimelineStats(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "seasons") String seasons
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

	@RequestMapping("/playerTimelinePerformance")
	public ModelAndView playerTimelinePerformance(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "seasons") String seasons
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
