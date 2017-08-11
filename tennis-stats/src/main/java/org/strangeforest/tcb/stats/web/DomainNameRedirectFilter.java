package org.strangeforest.tcb.stats.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.base.*;

@Component @Profile("openshift")
public class DomainNameRedirectFilter implements Filter {

	@Override public void init(FilterConfig filterConfig) {}
	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		if (httpRequest.getRequestURL().toString().contains("rhcloud.com")) {
			String url = "http://www.ultimatetennisstatistics.com";
			String servletPath = httpRequest.getServletPath();
			if (!Strings.isNullOrEmpty(servletPath) && !servletPath.equals("/"))
				url += servletPath;
			String queryString = httpRequest.getQueryString();
			if (!Strings.isNullOrEmpty(queryString))
				url += '?' + queryString;
			((HttpServletResponse)response).sendRedirect(url);
		}
		else
			chain.doFilter(request, response);
	}
}
