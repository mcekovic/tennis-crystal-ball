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
		return tournamentLevelTimeline(GRAND_SLAM, false);
	}

	@GetMapping("/mastersTimeline")
	public ModelAndView mastersTimeline() {
		return tournamentLevelTimeline(MASTERS, true);
	}

	private ModelAndView tournamentLevelTimeline(TournamentLevel level, boolean condensed) {
		TournamentLevelTimeline timeline = tournamentLevelService.getTournamentLevelTimeline(level.getCode(), condensed);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("name", level.getText());
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("condensed", condensed);
		return new ModelAndView("tournamentLevelTimeline", modelMap);
	}
}

