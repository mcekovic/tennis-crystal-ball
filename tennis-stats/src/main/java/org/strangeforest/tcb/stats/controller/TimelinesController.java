package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

@Controller
public class TimelinesController extends PageController {

	@Autowired private DominanceTimelineService timelineService;
	@Autowired private TournamentLevelService tournamentLevelService;
	@Autowired private RankingsService rankingsService;
	@Autowired private SurfaceService surfaceService;

	@GetMapping("dominanceTimeline")
	public ModelAndView dominanceTimeline(
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "averageElo", defaultValue = "false") boolean averageElo
	) {
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		DominanceTimeline timeline = timelineService.getDominanceTimeline().filterSeasons(seasonRange);
		int minGOATPoints = timelineService.getMinGOATPoints();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("fromSeason", seasonRange.hasLowerBound() ? seasonRange.lowerEndpoint() : null);
		modelMap.addAttribute("toSeason", seasonRange.hasUpperBound() ? seasonRange.upperEndpoint() : null);
		modelMap.addAttribute("averageElo", averageElo);
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("minGOATPoints", minGOATPoints);
		modelMap.addAttribute("dominanceRatioCoefficient", (int)DominanceSeason.DOMINANCE_RATIO_COEFFICIENT);
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

	@GetMapping("/topRankingsTimeline")
	public ModelAndView topRankingsTimeline(
		@RequestParam(name = "rankType", defaultValue = "POINTS") RankType rankType
	) {
		TopRankingsTimeline timeline = rankingsService.getTopRankingsTimeline(rankType);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("surfaces", Surface.values());
		return new ModelAndView("topRankingsTimeline", modelMap);
	}

	@GetMapping("/surfaceTimeline")
	public ModelAndView surfaceTimeline() {
		List<SurfaceTimelineItem> timeline = surfaceService.getSurfaceTimeline();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("surfaceTimeline", modelMap);
	}
}
