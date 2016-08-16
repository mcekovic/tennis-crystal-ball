package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class RankingsController extends PageController {

	@Autowired private PlayerService playerService;

	@RequestMapping("rankingsTable")
	public ModelAndView rankingsTable() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsTable", modelMap);
	}

	@RequestMapping({"allTimeEloRatings", "/eloRankingsTable"})
	public ModelAndView allTimeEloRatings() {
		return new ModelAndView("allTimeEloRatings", "allTimeElo", true);
	}

	@RequestMapping("/rankingsChart")
	public ModelAndView rankingsChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsChart", modelMap);
	}
}
