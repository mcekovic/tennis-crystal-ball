package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class BlogController extends PageController {

	@Autowired private GOATListService goatListService;

	@GetMapping("/blog")
	public ModelAndView blog() {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);
		return new ModelAndView("blog/blog", "goatTopN", goatTopN);
	}

	@GetMapping("/blog/eloKfactorTweaks")
	public ModelAndView eloKfactorTweaks() {
		return new ModelAndView("blog/eloKfactorTweaks");
	}
}
