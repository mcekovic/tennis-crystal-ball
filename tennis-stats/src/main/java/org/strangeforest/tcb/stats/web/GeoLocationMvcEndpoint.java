package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.endpoint.mvc.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.controler.*;

@Component @VisitorSupport
public class GeoLocationMvcEndpoint implements MvcEndpoint {

	@Autowired private VisitorRepository repository;

	@Override public String getPath() {
		return "/geolocationChart";
	}

	@Override public boolean isSensitive() {
		return false;
	}

	@Override public Class<? extends Endpoint> getEndpointType() {
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView geolocationChart(
		@RequestParam(value = "stat", defaultValue = "VISITS") VisitorStat stat,
		@RequestParam(value = "interval", defaultValue = "DAY") VisitorInterval interval
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
		return new ModelAndView("manage/geolocationChart", modelMap);
	}
}