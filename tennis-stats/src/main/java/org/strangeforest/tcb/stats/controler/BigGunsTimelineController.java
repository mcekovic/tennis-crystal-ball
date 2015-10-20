package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class BigGunsTimelineController {

	@Autowired private BigGunsTimelineService timelineService;

	@RequestMapping("/bigGunsTimeline")
	public ModelAndView bigGunsTimeline() {
		BigGunsTimeline timeline = timelineService.getBigGunsTimeline();
		return new ModelAndView("bigGunsTimeline", "timeline", timeline);
	}
}
