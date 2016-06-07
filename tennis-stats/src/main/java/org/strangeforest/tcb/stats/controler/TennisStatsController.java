package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

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

	@RequestMapping("/maintenance")
	public String maintenance() {
		return "maintenance";
	}
}
