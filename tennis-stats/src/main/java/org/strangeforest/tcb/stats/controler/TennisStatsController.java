package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class TennisStatsController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/rankingsChart")
	public String rankingsChart() {
		return "rankingsChart";
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}
}
