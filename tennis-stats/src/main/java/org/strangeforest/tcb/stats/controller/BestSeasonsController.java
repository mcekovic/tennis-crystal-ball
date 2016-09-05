package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class BestSeasonsController extends PageController {

	@Autowired private BestSeasonsService bestSeasonsService;

	@GetMapping("/bestSeasons")
	public ModelAndView bestSeasons() {
		int minSeasonGOATPoints = bestSeasonsService.getMinSeasonGOATPoints();
		return new ModelAndView("bestSeasons", "minSeasonGOATPoints", minSeasonGOATPoints);
	}
}
