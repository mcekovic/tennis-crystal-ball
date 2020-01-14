package org.strangeforest.tcb.stats.visitors;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.util.*;

import eu.bitwalker.useragentutils.*;

import static com.google.common.base.Strings.*;

@Component @VisitorSupport
public class GeoIPFilter implements Filter {

	@Autowired private VisitorManager visitorManager;

	private static final Map<BrowserType, Integer> MAX_REQUESTS = Map.of(
		BrowserType.TOOL, 10000,
		BrowserType.APP, 10000,
		BrowserType.UNKNOWN, 1000
	);

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
		BrowserType agentType = UserAgentUtil.getAgentType(httpRequest);
		Visitor visitor = visitorManager.visit(remoteAddr, agentType.name());
		Integer maxRequests = MAX_REQUESTS.get(agentType);
		if (maxRequests != null && visitor.getHits() > maxRequests) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;
			httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			return;
		}
		chain.doFilter(request, response);
	}
}
