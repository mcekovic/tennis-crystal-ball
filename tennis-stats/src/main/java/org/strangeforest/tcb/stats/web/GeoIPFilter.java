package org.strangeforest.tcb.stats.web;

import java.io.*;
import java.util.Optional;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.base.*;
import com.maxmind.geoip2.record.*;

@Component @Profile("!dev")
public class GeoIPFilter implements Filter {

	@Autowired private GeoIPService geoIPService;
	@Autowired private CounterService counterService;

	@Override public void init(FilterConfig filterConfig) {}
	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String remoteAddr = ((HttpServletRequest)request).getHeader("X-Forwarded-For");
		if (Strings.isNullOrEmpty(remoteAddr))
			remoteAddr = request.getRemoteAddr();
		int commaPos = remoteAddr.indexOf(',');
		if (commaPos > 0)
			remoteAddr = remoteAddr.substring(0, commaPos);
		Optional<Country> country = geoIPService.getCountry(remoteAddr);
		if (country.isPresent())
			counterService.increment("counter.country." + country.get().getName());
		chain.doFilter(request, response);
	}
}
