package org.strangeforest.tcb.stats.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.stereotype.*;

import static com.google.common.base.Strings.*;

@Component @VisitorSupport
public class GeoIPFilter implements Filter {

	@Autowired private VisitorManager visitorManager;
	@Autowired private CounterService counterService;

	@Override public void init(FilterConfig filterConfig) {}
	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String remoteAddr = ((HttpServletRequest)request).getHeader("X-Forwarded-For");
		if (isNullOrEmpty(remoteAddr))
			remoteAddr = request.getRemoteAddr();
		int commaPos = remoteAddr.indexOf(',');
		if (commaPos > 0)
			remoteAddr = remoteAddr.substring(0, commaPos);
		String country = visitorManager.visit(remoteAddr).getCountry();
		if (!isNullOrEmpty(country))
			counterService.increment("counter.country." + country);
		chain.doFilter(request, response);
	}
}
