package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

@Controller
public class TournamentLevelTimelineController extends PageController {

	@Autowired
	private TournamentLevelService tournamentLevelService;

	@GetMapping("/tournamentLevelTimeline")
	public ModelAndView tournamentLevelTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevel tournamentLevel = TournamentLevel.decode(level);
		TournamentLevelTimeline timeline = tournamentLevelService.getTournamentLevelTimeline(level, tournamentLevel != MASTERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("tournamentLevelTimeline", modelMap);
	}

	@GetMapping("/teamTournamentLevelTimeline")
	public ModelAndView teamTournamentLevelTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevel tournamentLevel = TournamentLevel.decode(level);
		List<TeamTournamentLevelTimelineItem> timeline = tournamentLevelService.getTeamTournamentLevelTimeline(level);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("teamTournamentLevelTimeline", modelMap);
	}
}

