package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static java.lang.Boolean.*;

@Controller
public class RankingsController extends PageController {

	@Autowired private PlayerService playerService;

	@GetMapping("/rankingsTable")
	public ModelAndView rankingsTable(
		@RequestParam(name = "rankType", required = false) RankType rankType,
		@RequestParam(name = "season", required = false) Integer season
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("season", season);
		return new ModelAndView("rankingsTable", modelMap);
	}

	@GetMapping({"/peakEloRatings", "/allTimeEloRatings"})
	public ModelAndView peakEloRatings() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("peakElo", TRUE);
		return new ModelAndView("peakEloRatings", modelMap);
	}

	@GetMapping("/rankingsChart")
	public ModelAndView rankingsChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("rankTypes", RankType.values());
		return new ModelAndView("rankingsChart", modelMap);
	}
}
