package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TopPerformersController extends PageController {

	@Autowired private TopPerformersService topPerformersService;
	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;

	@GetMapping("/topPerformers")
	public ModelAndView topPerformers(
		@RequestParam(name = "category", required = false) String category
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("category", category);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getBasicCategoryClasses());
		modelMap.addAttribute("seasons", topPerformersService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournamentService.getTournaments());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", matchesService.getCountries());
		return new ModelAndView("topPerformers", modelMap);
	}
}
