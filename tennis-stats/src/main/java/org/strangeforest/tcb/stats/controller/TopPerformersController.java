package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TopPerformersController extends PageController {

	@Autowired private TopPerformersService topPerformersService;

	@GetMapping("/topPerformers")
	public ModelAndView topPerformers(
		@RequestParam(name = "category", required = false) String category
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("category", category);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", topPerformersService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		return new ModelAndView("topPerformers", modelMap);
	}
}
