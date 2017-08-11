package org.strangeforest.tcb.stats.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import static com.google.common.base.Strings.*;

@Component @Profile("openshift")
public class DomainNameRedirectFilter implements Filter {

	@Override public void init(FilterConfig filterConfig) {}
	@Override public void destroy() {}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		if (httpRequest.getRequestURL().toString().contains("rhcloud.com") && !"true".equals(httpRequest.getParameter("redirected"))) {
			String url = "http://www.ultimatetennisstatistics.com";
			String servletPath = httpRequest.getServletPath();
			boolean isRootPath = isNullOrEmpty(servletPath) || servletPath.equals("/");
			if (!isRootPath)
				url += servletPath;
			String queryString = httpRequest.getQueryString();
			if (!(isRootPath && isNullOrEmpty(queryString))) {
				url += '?';
				if (!isNullOrEmpty(queryString)) {
					url += queryString;
					if (!isRootPath)
						url += '&';
				}
				if (!isRootPath)
					url += "redirected=true";
			}
			HttpServletResponse httpResponse = (HttpServletResponse)response;
			httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			httpResponse.setHeader("Location", url);
		}
		else
			chain.doFilter(request, response);
	}
}
