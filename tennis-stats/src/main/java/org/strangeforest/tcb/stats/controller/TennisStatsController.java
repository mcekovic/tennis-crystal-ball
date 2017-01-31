package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TennisStatsController extends PageController {

	@Autowired private GOATListService goatListService;
	@Autowired private RankingsService rankingsService;

	@GetMapping("/")
	public ModelAndView index() {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);
		return new ModelAndView("index", "goatTopN", goatTopN);
	}

	@GetMapping("/about")
	public ModelAndView about() {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);
		return new ModelAndView("about", "goatTopN", goatTopN);
	}

	@GetMapping("/rankingTopN")
	public ModelAndView rankingTopN(
      @RequestParam(name = "rankType") RankType rankType
	) {
		List<PlayerRanking> rankingTopN = rankingsService.getRankingsTopN(rankType, 10);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("rankingTopN", rankingTopN);
		return new ModelAndView("rankingTopN", modelMap);
	}

	@GetMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@GetMapping("/liveScores")
	public String liveScores() {
		return "liveScores";
	}

	@Value("${tennis-stats.down-for-maintenance:false}")
	private boolean downForMaintenance;

	@Value("${tennis-stats.down-for-maintenance.message:}")
	private String maintenanceMessage;

	@GetMapping("/maintenance")
	public ModelAndView maintenance() {
		return downForMaintenance
			? new ModelAndView("maintenance", "maintenanceMessage", maintenanceMessage)
			: new ModelAndView("index");
	}
}
