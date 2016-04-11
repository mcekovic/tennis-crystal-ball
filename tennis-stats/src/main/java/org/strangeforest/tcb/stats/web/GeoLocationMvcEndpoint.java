package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.endpoint.mvc.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

@Component @VisitorSupport
public class GeoLocationMvcEndpoint implements MvcEndpoint {

	@Autowired private MetricsEndpoint metricsEndpoint;

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
		@RequestParam(value = "chartBy", defaultValue = "hits") String chartBy
	) {
		String caption = StringUtils.capitalize(chartBy);
		String prefix = GeoIPFilter.COUNTER_COUNTRY + chartBy + '.';
		Object[] countries = metricsEndpoint.invoke().entrySet().stream()
			.filter(e -> e.getKey().startsWith(prefix))
			.map(e -> new Object[] {e.getKey().substring(prefix.length()), e.getValue()})
			.collect(() -> {
				ArrayList<Object[]> list = new ArrayList<>();
				list.add(new Object[] {"Country", caption});
				return list;
			}, List::add, List::addAll).toArray();

		ModelMap modelMap = new ModelMap();
		modelMap.put("chartBy", chartBy);
		modelMap.put("countries", countries);
		return new ModelAndView("manage/geolocationChart", modelMap);
	}
}