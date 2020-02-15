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

	@Autowired private PlayerService playerService;
	@Autowired private TopPerformersService topPerformersService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private DataService dataService;

	@GetMapping("/performanceChart")
	public ModelAndView performanceChart(
		@RequestParam(name = "players", required = false) String players,
		@RequestParam(name = "category", required = false) String category,
		@RequestParam(name = "chartType", required = false) String chartType,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = F) Boolean byAge
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("players", players);
		modelMap.addAttribute("category", category);
		modelMap.addAttribute("chartType", chartType);
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		modelMap.addAttribute("byAge", byAge);
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		modelMap.addAttribute("chartTypes", PerformanceChartService.PerformanceChartType.values());
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

	@GetMapping("/resultsChart")
	public ModelAndView resultsChart(
		@RequestParam(name = "players", required = false) String players,
		@RequestParam(name = "result", defaultValue = "W") String result,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "bySeason", defaultValue = F) boolean bySeason,
		@RequestParam(name = "byAge", defaultValue = F) Boolean byAge
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("players", players);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		modelMap.addAttribute("bySeason", bySeason);
		modelMap.addAttribute("byAge", byAge);
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("resultsChart", modelMap);
	}
}
