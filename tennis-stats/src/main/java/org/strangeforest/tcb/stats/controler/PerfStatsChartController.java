package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

@Controller
public class PerfStatsChartController extends PageController {

	@Autowired private TopPerformersService topPerformersService;
	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;

	@RequestMapping("/performanceChart")
	public ModelAndView performanceChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", topPerformersService.getSeasons());
		return new ModelAndView("performanceChart", modelMap);
	}

	@RequestMapping("/statsChart")
	public ModelAndView statsChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("categoryTypes",  StatsCategory.getCategories().entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getType().name())));
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("seasons", statisticsService.getSeasons());
		return new ModelAndView("statsChart", modelMap);
	}
}
