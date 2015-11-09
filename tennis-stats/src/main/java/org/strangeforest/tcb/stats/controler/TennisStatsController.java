package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class TennisStatsController extends BaseController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@RequestMapping("/rankingsChart")
	public String rankingsChart() {
		return "rankingsChart";
	}
}
