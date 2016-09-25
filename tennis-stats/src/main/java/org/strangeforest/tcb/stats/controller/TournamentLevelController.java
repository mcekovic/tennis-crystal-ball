package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

@Controller
public class TournamentLevelController extends PageController {

	@Autowired
	private TournamentLevelService tournamentLevelService;

	@GetMapping("/grandSlamTimeline")
	public ModelAndView grandSlamTimeline() {
		return tournamentLevelTimeline(GRAND_SLAM);
	}

	@GetMapping("/tourFinalsTimeline")
	public ModelAndView tourFinalsTimeline() {
		return tournamentLevelTimeline(TOUR_FINALS);
	}

	@GetMapping("/mastersTimeline")
	public ModelAndView mastersTimeline() {
		return tournamentLevelTimeline(MASTERS);
	}

	@GetMapping("/olympicsTimeline")
	public ModelAndView olympicsTimeline() {
		return tournamentLevelTimeline(OLYMPICS);
	}

	private ModelAndView tournamentLevelTimeline(TournamentLevel level) {
		TournamentLevelTimeline timeline = tournamentLevelService.getTournamentLevelTimeline(level.getCode(), level != MASTERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("tournamentLevelTimeline", modelMap);
	}
}

