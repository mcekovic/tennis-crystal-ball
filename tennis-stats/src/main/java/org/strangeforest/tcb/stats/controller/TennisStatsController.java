package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TennisStatsController extends PageController {

	@Autowired private GOATListService goatListService;

	@RequestMapping("/")
	public ModelAndView index() {
		List<PlayerRanking> goatTop10 = goatListService.getGOATTopN(10);
		return new ModelAndView("index", "goatTop10", goatTop10);
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
