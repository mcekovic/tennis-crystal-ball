package org.strangeforest.tcb.stats.visitors;

import java.io.*;
import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.util.*;

import com.github.benmanes.caffeine.cache.*;
import eu.bitwalker.useragentutils.*;

import static com.google.common.base.Strings.*;
import static com.google.common.net.HttpHeaders.*;

@Component @VisitorSupport
public class VisitorFilter implements Filter {

	@Autowired private VisitorManager visitorManager;
	private Cache<String, Boolean> blockedVisitors;

	private static final Logger LOGGER = LoggerFactory.getLogger(VisitorFilter.class);


	@Override public void init(FilterConfig filterConfig) {
		blockedVisitors = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
	}

	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		var httpRequest = (HttpServletRequest)request;
		var remoteAddr = getRemoteAddress(httpRequest);
		var agentType = UserAgentUtil.getAgentType(httpRequest);
		var visit = visitorManager.visit(remoteAddr, agentType.name());
		if (visit.isAllowed())
			chain.doFilter(request, response);
		else
			tooManyRequests(httpRequest, response, visit);
	}

	private static String getRemoteAddress(HttpServletRequest httpRequest) {
		var remoteAddr = httpRequest.getHeader(X_FORWARDED_FOR);
		if (isNullOrEmpty(remoteAddr))
			remoteAddr = httpRequest.getRemoteAddr();
		var commaPos = remoteAddr.indexOf(',');
		if (commaPos > 0)
			remoteAddr = remoteAddr.substring(0, commaPos);
		return remoteAddr;
	}

	private void tooManyRequests(HttpServletRequest httpRequest, ServletResponse response, Visit visit) {
		var httpResponse = (HttpServletResponse)response;
		httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		if (blockedVisitors.asMap().putIfAbsent(visit.visitor.getIpAddress(), Boolean.TRUE) == null)
			LOGGER.info("Visitor has been blocked: {}, reason: {}, user-agent: {}", visit.visitor, visit.message, httpRequest.getHeader("User-Agent"));
	}
}
