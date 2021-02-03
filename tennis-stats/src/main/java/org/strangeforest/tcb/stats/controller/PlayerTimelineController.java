package org.strangeforest.tcb.stats.controller;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

@Controller
public class PlayerTimelineController extends BaseController {

	@Autowired private RankingsService rankingsService;
	@Autowired private PerformanceService performanceService;
	@Autowired private StatisticsService statisticsService;

	@GetMapping("/playerTimelineRanking")
	public ModelAndView playerTimelineRanking(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		var timeline = rankingsService.getPlayerRankingTimeline(playerId);
		var seasonList = toSeasons(seasons);
		ensureSeasons(timeline.getSeasonsWeeksAtRank(), seasonList, WeeksAtRank.EMPTY);

		var modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("seasons", seasonList);
		return new ModelAndView("playerTimelineRanking", modelMap);
	}

	@GetMapping("/playerTimelineEloRanking")
	public ModelAndView playerTimelineEloRanking(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "rankType", defaultValue = "ELO_RANK") RankType rankType,
		@RequestParam(name = "seasons") String seasons
	) {
		var timeline = rankingsService.getPlayerEloRankingTimeline(playerId, rankType);
		var seasonList = toSeasons(seasons);
		ensureSeasons(timeline.getSeasonsWeeksAtRank(), seasonList, WeeksAtRank.EMPTY);

		var modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("seasons", seasonList);
		return new ModelAndView("playerTimelineRanking", modelMap);
	}

	@GetMapping("/playerTimelinePerformance")
	public ModelAndView playerTimelinePerformance(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		var careerPerf = performanceService.getPlayerPerformance(playerId);
		var seasonList = toSeasons(seasons);
		var seasonsPerf = performanceService.getPlayerSeasonsPerformance(playerId);
		ensureSeasons(seasonsPerf, seasonList, PlayerPerformance.EMPTY);

		var modelMap = new ModelMap();
		modelMap.addAttribute("careerPerf", careerPerf);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsPerf", seasonsPerf);
		return new ModelAndView("playerTimelinePerformance", modelMap);
	}

	@GetMapping("/playerTimelineStats")
	public ModelAndView playerTimelineStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		var careerStats = statisticsService.getPlayerStats(playerId);
		var seasonList = toSeasons(seasons);
		var seasonsStats = statisticsService.getPlayerSeasonsStats(playerId);
		ensureSeasons(seasonsStats, seasonList, PlayerStats.EMPTY);

		var modelMap = new ModelMap();
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("careerStats", careerStats);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsStats", seasonsStats);
		return new ModelAndView("playerTimelineStats", modelMap);
	}

	private List<Integer> toSeasons(String seasons) {
		return Stream.of(seasons.split(",")).map(Integer::valueOf).collect(toList());
	}

	private <T> void ensureSeasons(Map<Integer, T> seasonsData, List<Integer> seasons, T empty) {
		for (var season : seasons) {
			if (!seasonsData.containsKey(season))
				seasonsData.put(season, empty);
		}
	}
}
