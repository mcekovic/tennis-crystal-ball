package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;

@Controller
public class PerfStatsChartController extends PageController {

	@Autowired private TopPerformersService topPerformersService;
	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;

	@GetMapping("/performanceChart")
	public ModelAndView performanceChart(
		@RequestParam(name = "players", required = false) String players,
		@RequestParam(name = "category", required = false) String category,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = F) Boolean byAge
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("players", players);
		modelMap.addAttribute("category", category);
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		modelMap.addAttribute("byAge", byAge);
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", topPerformersService.getSeasons());
		return new ModelAndView("performanceChart", modelMap);
	}

	@GetMapping("/statsChart")
	public ModelAndView statsChart(
		@RequestParam(name = "players", required = false) String players,
		@RequestParam(name = "category", required = false) String category,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = F) Boolean byAge
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("players", players);
		modelMap.addAttribute("category", category);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		modelMap.addAttribute("byAge", byAge);
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("categoryTypes", StatsCategory.getCategoryTypes());
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("seasons", statisticsService.getSeasons());
		return new ModelAndView("statsChart", modelMap);
	}
}
