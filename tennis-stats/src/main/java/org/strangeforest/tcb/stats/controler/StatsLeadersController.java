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
public class StatsLeadersController extends BaseController {

	@Autowired private StatsLeadersService statsLeadersService;

	@RequestMapping("/statsLeaders")
	public ModelAndView statsLeaders() {
		List<Integer> seasons = statsLeadersService.getSeasons();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("surfaces", Options.SURFACES);
		return new ModelAndView("statsLeaders", modelMap);
	}
}
