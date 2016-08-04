package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class StatsLeadersController extends PageController {

	@Autowired private StatisticsService statisticsService;

	@RequestMapping("/statsLeaders")
	public ModelAndView statsLeaders() {
		List<Integer> seasons = statisticsService.getSeasons();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("surfaces", Surface.values());
		return new ModelAndView("statsLeaders", modelMap);
	}
}
