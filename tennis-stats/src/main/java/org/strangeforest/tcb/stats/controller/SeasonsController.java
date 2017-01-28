package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class SeasonsController extends PageController {

	@Autowired private SeasonsService seasonsService;

	private static final int MAX_RECORD_PLAYERS = 10;

	@GetMapping("/seasons")
	public String seasons() {
		return "seasons";
	}

	@GetMapping("/season")
	public ModelAndView season(
		@RequestParam(name = "season") int season
	) {
		List<RecordDetailRow> seasonTitles = seasonsService.getSeasonRecord(season, "W", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> seasonFinals = seasonsService.getSeasonRecord(season, "F", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> seasonSemiFinals = seasonsService.getSeasonRecord(season, "SF", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> seasonAppearances = seasonsService.getSeasonRecord(season, "RR", MAX_RECORD_PLAYERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("seasonTitles", seasonTitles);
		modelMap.addAttribute("seasonFinals", seasonFinals);
		modelMap.addAttribute("seasonSemiFinals", seasonSemiFinals);
		modelMap.addAttribute("seasonAppearances", seasonAppearances);
		return new ModelAndView("season", modelMap);
	}

	@GetMapping("/seasonRankings")
	public ModelAndView seasonRankings(
		@RequestParam(name = "season") int season
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("surfaces", Surface.values());
		return new ModelAndView("seasonRankings", modelMap);
	}

	@GetMapping("/seasonPerformance")
	public ModelAndView seasonPerformance(
		@RequestParam(name = "season") int season
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		return new ModelAndView("seasonPerformance", modelMap);
	}

	@GetMapping("/seasonStats")
	public ModelAndView seasonStats(
		@RequestParam(name = "season") int season
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		return new ModelAndView("seasonStats", modelMap);
	}

	@GetMapping("/bestSeasons")
	public ModelAndView bestSeasons() {
		int minSeasonGOATPoints = seasonsService.getMinSeasonGOATPoints();
		return new ModelAndView("bestSeasons", "minSeasonGOATPoints", minSeasonGOATPoints);
	}
}
