package org.strangeforest.tcb.util;

import javax.servlet.http.*;

import eu.bitwalker.useragentutils.*;

public abstract class UserAgentUtil {

	public static BrowserType getAgentType(HttpServletRequest httpRequest) {
		return UserAgent.parseUserAgentString(httpRequest.getHeader("User-Agent")).getBrowser().getBrowserType();
	}
}
