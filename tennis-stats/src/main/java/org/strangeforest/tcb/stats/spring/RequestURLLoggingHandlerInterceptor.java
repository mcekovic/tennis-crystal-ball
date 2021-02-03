package org.strangeforest.tcb.stats.spring;

import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.web.servlet.handler.*;
import org.strangeforest.tcb.util.*;

import com.google.common.base.*;

public class RequestURLLoggingHandlerInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestURLLoggingHandlerInterceptor.class);
	
	@Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex != null)
			LOGGER.warn("Request that generated exception: {}\nRequest URL: {}\nAgent Type: {}\nUser-Agent: {}", ex.getMessage(), getRequestURL(request), UserAgentUtil.getAgentType(request), getUserAgent(request));
	}

	private static String getRequestURL(HttpServletRequest request) {
		var url = request.getRequestURL();
		var params = request.getQueryString();
		if (!Strings.isNullOrEmpty(params))
			url.append('?').append(replacePatternBreakingChars(params));
		return replacePatternBreakingChars(url.toString());
	}

	private String getUserAgent(HttpServletRequest request) {
		var userAgent = request.getHeader("User-Agent");
		return !Strings.isNullOrEmpty(userAgent) ? replacePatternBreakingChars(userAgent) : "N/A";
	}

	private static String replacePatternBreakingChars(String s) {
		return s.replaceAll("[\n|\r|\t]", " ");
	}
}
