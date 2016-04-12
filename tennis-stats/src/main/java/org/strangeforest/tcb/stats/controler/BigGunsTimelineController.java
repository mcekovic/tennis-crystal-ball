package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class BigGunsTimelineController extends PageController {

	@Autowired private BigGunsTimelineService timelineService;

	@RequestMapping("/bigGunsTimeline")
	public ModelAndView bigGunsTimeline() {
		BigGunsTimeline timeline = timelineService.getBigGunsTimeline();
		int minGOATPoints = timelineService.getMinGOATPoints();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("minGOATPoints", minGOATPoints);
		return new ModelAndView("bigGunsTimeline", modelMap);
	}
}
