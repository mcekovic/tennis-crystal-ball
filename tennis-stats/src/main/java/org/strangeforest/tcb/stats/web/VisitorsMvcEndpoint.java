package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.mvc.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.controller.*;

@Component @VisitorSupport
public class VisitorsMvcEndpoint extends AbstractMvcEndpoint {

	@Autowired private VisitorRepository repository;

	public VisitorsMvcEndpoint() {
		super("/visitorsChart", false);
	}

	@GetMapping(produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView visitorsChart(
		@RequestParam(name = "stat", defaultValue = "VISITS") VisitorStat stat,
		@RequestParam(name = "interval", defaultValue = "DAY") VisitorInterval interval
	) {
		List<Object[]> countries = repository.getVisitorsByCountry(stat, interval);
		countries.add(0, new Object[] {"Country", stat.getCaption()});

		ModelMap modelMap = new ModelMap();
		modelMap.put("versions", BaseController.VERSIONS);
		modelMap.put("stat", stat);
		modelMap.put("interval", interval);
		modelMap.put("stats", VisitorStat.values());
		modelMap.put("intervals", VisitorInterval.values());
		modelMap.put("countries", countries.toArray());
		return new ModelAndView("manage/visitorsChart", modelMap);
	}
}