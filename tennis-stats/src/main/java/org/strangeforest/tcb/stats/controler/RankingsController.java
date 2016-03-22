package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class RankingsController extends BaseController {

	@Autowired private DataService dataService;

	@RequestMapping({"/rankingsTable", "/eloRankingsTable"})
	public ModelAndView rankingsTable() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		if (Objects.equals(getServletPath(), "/eloRankingsTable"))
			modelMap.addAttribute("rankType", RankType.ELO_RATING.name());
		return new ModelAndView("rankingsTable", modelMap);
	}

	@RequestMapping("/rankingsChart")
	public ModelAndView rankingsChart() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsChart", modelMap);
	}
}
