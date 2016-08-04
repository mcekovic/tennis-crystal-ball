package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

@Controller
public class TennisStatsController extends PageController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@Value("${tennis-stats.down-for-maintenance.message:}")
	private String maintenanceMessage;

	@RequestMapping("/maintenance")
	public ModelAndView maintenance() {
		return new ModelAndView("maintenance", "maintenanceMessage", maintenanceMessage);
	}
}
