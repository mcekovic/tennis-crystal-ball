package org.strangeforest.tcb.util;

import java.util.*;
import javax.servlet.http.*;

import eu.bitwalker.useragentutils.*;

public abstract class UserAgentUtil {

	public static final EnumSet<BrowserType> ROBOTS_AND_UNKNOWN = EnumSet.of(BrowserType.ROBOT, BrowserType.UNKNOWN);

	public static BrowserType getAgentType(HttpServletRequest httpRequest) {
		return UserAgent.parseUserAgentString(httpRequest.getHeader("User-Agent")).getBrowser().getBrowserType();
	}
}
