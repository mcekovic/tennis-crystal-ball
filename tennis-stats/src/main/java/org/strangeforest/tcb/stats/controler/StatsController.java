package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class StatsController {

	@Autowired private PlayerStatsService statsService;

	@RequestMapping("/matchStats")
	public ModelAndView matchStats(@RequestParam(value = "matchId") long matchId) {
		MatchStats matchStats = statsService.getMatchStats(matchId);
		return new ModelAndView("matchStats", "matchStats", matchStats);
	}
}
