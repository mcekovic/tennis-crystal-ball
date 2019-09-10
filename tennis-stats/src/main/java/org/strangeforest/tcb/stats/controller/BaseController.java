package org.strangeforest.tcb.stats.controller;

import java.util.*;
import javax.annotation.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.core.env.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;
import eu.bitwalker.useragentutils.*;

public abstract class BaseController {

	@Autowired private Environment environment;
	private String googleAnalyticsPropertyId;
	private String googleAdSenseClientId;
	private boolean digitalOcean;

	public static final Map<String, String> VERSIONS = ImmutableMap.of(
		"jquery", "2.2.4",
		"jquery-ui", "1.12.1",
		"bootstrap", "3.4.0",
		"bootgrid", "1.3.1",
		"font-awesome", "4.7.0"
	);

	@PostConstruct
	public void init() {
		digitalOcean = environment.acceptsProfiles(Profiles.of("digitalocean"));
		googleAnalyticsPropertyId = environment.getProperty("google.analytics.property-id");
		googleAdSenseClientId = environment.getProperty("google.adsense.client-id");
	}

	@ModelAttribute("versions")
	public Map<String, String> getVersions() {
		return VERSIONS;
	}

	@ModelAttribute("digitalOcean")
	public boolean isDigitalOcean() {
		return digitalOcean;
	}

	@ModelAttribute("googleAnalyticsPropertyId")
	public String getGoogleAnalyticsPropertyId() {
		return googleAnalyticsPropertyId;
	}

	@ModelAttribute("googleAdSenseClientId")
	public String getGoogleAdSenseClientId() {
		return googleAdSenseClientId;
	}

	protected static void rejectRobots(HttpServletRequest httpRequest) {
		if (UserAgentUtil.getAgentType(httpRequest) == BrowserType.ROBOT)
			throw new ForbiddenException("Not accessible by robots");
	}
}
