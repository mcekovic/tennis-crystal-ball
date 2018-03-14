package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class BlogController extends PageController {

	@Autowired private GOATListService goatListService;

	@GetMapping("/blog")
	public ModelAndView blog(
		@RequestParam(name = "post", defaultValue = "newBlogSection") String post
	) {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(20);
		
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("post", post);
		modelMap.addAttribute("goatTopN", goatTopN);
		return new ModelAndView("blog/blog", modelMap);
	}

	@GetMapping("/blog/{post}")
	public ModelAndView post(
		@PathVariable String post
	) {
		return new ModelAndView("blog/" + post);
	}
}
