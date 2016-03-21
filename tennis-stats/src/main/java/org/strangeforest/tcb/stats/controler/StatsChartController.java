package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class StatsChartController extends BaseController {

	@Autowired
	private DataService dataService;

	@RequestMapping("/statsChart")
	public ModelAndView statsChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("statsChart", modelMap);
	}
}
