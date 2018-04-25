package org.strangeforest.tcb.stats.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import eu.bitwalker.useragentutils.*;
import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import static com.google.common.base.Strings.*;

@Component @VisitorSupport
public class GeoIPFilter implements Filter {

	@Autowired private VisitorManager visitorManager;
	@Autowired private MeterRegistry meterRegistry;

	private static final String VISITORS = "visitors";
	private static final String COUNTRY = "country";

	@Override public void init(FilterConfig filterConfig) {}
	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String remoteAddr = httpRequest.getHeader("X-Forwarded-For");
		if (isNullOrEmpty(remoteAddr))
			remoteAddr = request.getRemoteAddr();
		int commaPos = remoteAddr.indexOf(',');
		if (commaPos > 0)
			remoteAddr = remoteAddr.substring(0, commaPos);
		String agentType = getAgentType(httpRequest).name();
		Visitor visitor = visitorManager.visit(remoteAddr, agentType);
		String country = visitor.getCountry();
		if (!isNullOrEmpty(country))
			meterRegistry.counter(VISITORS, COUNTRY, country).increment();
		chain.doFilter(request, response);
	}

	private static BrowserType getAgentType(HttpServletRequest httpRequest) {
		return UserAgent.parseUserAgentString(httpRequest.getHeader("User-Agent")).getBrowser().getBrowserType();
	}
}
