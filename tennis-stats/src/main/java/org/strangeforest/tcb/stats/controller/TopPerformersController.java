package org.strangeforest.tcb.stats.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TopPerformersController extends PageController {

	@Autowired private TopPerformersService topPerformersService;

	@RequestMapping("/topPerformers")
	public ModelAndView topPerformers() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", topPerformersService.getSeasons());
		return new ModelAndView("topPerformers", modelMap);
	}
}
