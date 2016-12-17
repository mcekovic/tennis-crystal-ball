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
public class TimelinesController extends PageController {

	@Autowired private DominanceTimelineService timelineService;
	@Autowired private TournamentLevelService tournamentLevelService;
	@Autowired private SurfaceService surfaceService;

	@GetMapping({"dominanceTimeline", "/bigGunsTimeline"})
	public ModelAndView dominanceTimeline() {
		DominanceTimeline timeline = timelineService.getDominanceTimeline();
		int minGOATPoints = timelineService.getMinGOATPoints();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("minGOATPoints", minGOATPoints);
		return new ModelAndView("dominanceTimeline", modelMap);
	}

	@GetMapping("/tournamentLevelTimeline")
	public ModelAndView tournamentLevelTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevel tournamentLevel = decode(level);
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
		TournamentLevel tournamentLevel = decode(level);
		List<TeamTournamentLevelTimelineItem> timeline = tournamentLevelService.getTeamTournamentLevelTimeline(level);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("teamTournamentLevelTimeline", modelMap);
	}

	@GetMapping("/surfaceTimeline")
	public ModelAndView surfaceTimeline() {
		List<SurfaceTimelineItem> timeline = surfaceService.getSurfaceTimeline();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("surfaceTimeline", modelMap);
	}
}
