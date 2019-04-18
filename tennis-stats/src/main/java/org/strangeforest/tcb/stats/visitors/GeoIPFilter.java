package org.strangeforest.tcb.stats.visitors;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.util.*;

import static com.google.common.base.Strings.*;

@Component @VisitorSupport
public class GeoIPFilter implements Filter {

	@Autowired private VisitorManager visitorManager;

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
		String agentType = UserAgentUtil.getAgentType(httpRequest).name();
		visitorManager.visit(remoteAddr, agentType);
		chain.doFilter(request, response);
	}
}
