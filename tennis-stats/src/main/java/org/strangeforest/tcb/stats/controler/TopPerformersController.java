package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TopPerformersController {

	@Autowired private TopPerformersService topPerformersService;

	@RequestMapping("/topPerformers")
	public ModelAndView topPerformers() {
		List<Integer> seasons = topPerformersService.getSeasons();
		return new ModelAndView("topPerformers", "seasons", seasons);
	}
}
