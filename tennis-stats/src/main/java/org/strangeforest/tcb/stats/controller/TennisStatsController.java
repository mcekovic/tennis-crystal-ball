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

	@RequestMapping("/")
	public ModelAndView index() {
		List<PlayerRanking> goatTop10 = goatListService.getGOATTopN(10);
		List<PlayerRanking> rankingTop10 = rankingsService.getRankingsTopN(RankType.POINTS, 10);
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("goatTop10", goatTop10);
		modelMap.addAttribute("rankingTop10", rankingTop10);
		return new ModelAndView("index", modelMap);
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@Value("${tennis-stats.down-for-maintenance:false}")
	private boolean downForMaintenance;

	@Value("${tennis-stats.down-for-maintenance.message:}")
	private String maintenanceMessage;

	@RequestMapping("/maintenance")
	public ModelAndView maintenance() {
		return downForMaintenance
			? new ModelAndView("maintenance", "maintenanceMessage", maintenanceMessage)
			: new ModelAndView("index");
	}
}
