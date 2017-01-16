package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class SeasonsController extends PageController {

	@Autowired private SeasonsService seasonsService;

	@GetMapping("/seasons")
	public ModelAndView seasons() {
		List<Season> seasons = seasonsService.getSeasons();
		return new ModelAndView("seasons", "seasons", seasons);
	}

	@GetMapping("/bestSeasons")
	public ModelAndView bestSeasons() {
		int minSeasonGOATPoints = seasonsService.getMinSeasonGOATPoints();
		return new ModelAndView("bestSeasons", "minSeasonGOATPoints", minSeasonGOATPoints);
	}
}
