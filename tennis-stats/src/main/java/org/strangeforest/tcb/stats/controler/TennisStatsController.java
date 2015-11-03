package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TennisStatsController {

	@Autowired private DataService dataService;

	@RequestMapping("/")
	public ModelAndView index() {
		return new ModelAndView("index", "lastUpdate", dataService.getLastUpdate());
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@RequestMapping("/rankingsChart")
	public String rankingsChart() {
		return "rankingsChart";
	}

	@RequestMapping("/topPerformers")
	public String topPerformers() {
		return "topPerformers";
	}

	@RequestMapping("/statsLeaders")
	public String statsLeaders() {
		return "statsLeaders";
	}
}
