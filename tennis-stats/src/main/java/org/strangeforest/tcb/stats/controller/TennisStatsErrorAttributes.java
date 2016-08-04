package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.boot.autoconfigure.web.*;
import org.springframework.web.context.request.*;

public class TennisStatsErrorAttributes extends DefaultErrorAttributes {

	@Override public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
		errorAttributes.put("versions", BaseController.VERSIONS);
		return errorAttributes;
	}
}
