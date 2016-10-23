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
	public ModelAndView rankingsTable() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsTable", modelMap);
	}

	@GetMapping("/allTimeEloRatings")
	public ModelAndView allTimeEloRatings() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("allTimeElo", TRUE);
		return new ModelAndView("allTimeEloRatings", modelMap);
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
