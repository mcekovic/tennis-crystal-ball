package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class TennisStatsController extends PageController {

	@Value("${tennis-stats.down-for-maintenance:false}")
	private boolean downForMaintenance;

	@RequestMapping("/")
	public String index() {
		return downForMaintenance ? "maintenance" : "index";
	}

	@RequestMapping("/goatList")
	public String goatList() {
		return "goatList";
	}
}
