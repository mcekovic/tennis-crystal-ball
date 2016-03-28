package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class RankingsController extends BaseController {

	@Autowired private DataService dataService;

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
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsChart", modelMap);
	}
}
