package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.endpoint.mvc.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

@Component @Profile("!dev")
public class GeoLocationMvcEndpoint implements MvcEndpoint {

	@Autowired private MetricsEndpoint metricsEndpoint;

	private static final String COUNTER_COUNTRY_PREFIX = "counter.country.";

	@Override public String getPath() {
		return "/geolocation";
	}

	@Override public boolean isSensitive() {
		return false;
	}

	@Override public Class<? extends Endpoint> getEndpointType() {
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView geolocation() {
		Object[] countries = metricsEndpoint.invoke().entrySet().stream()
			.filter(e -> e.getKey().startsWith(COUNTER_COUNTRY_PREFIX))
			.map(e -> new Object[] {e.getKey().substring(COUNTER_COUNTRY_PREFIX.length()), e.getValue()})
			.collect(() -> {
				ArrayList<Object[]> list = new ArrayList<>();
				list.add(new Object[] {"Country", "Hits"});
				return list;
			}, List::add, List::addAll).toArray();
		return new ModelAndView("manage/geolocationChart", "countries", countries);
	}
}