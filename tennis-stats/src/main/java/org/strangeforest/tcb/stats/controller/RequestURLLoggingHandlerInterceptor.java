package org.strangeforest.tcb.stats.controller;

import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.web.servlet.handler.*;

import com.google.common.base.*;

public class RequestURLLoggingHandlerInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestURLLoggingHandlerInterceptor.class);
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		if (ex != null)
			LOGGER.warn("Request that generated exception: {}\nRequest URL: {}\nUser-Agent: {}", ex.getMessage(), getRequestURL(request), request.getHeader("User-Agent"));
	}

	private static String getRequestURL(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String params = request.getQueryString();
		if (!Strings.isNullOrEmpty(params))
			url.append('?').append(params);
		return url.toString();
	}
}
