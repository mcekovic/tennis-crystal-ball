package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.boot.autoconfigure.web.*;
import org.springframework.web.context.request.*;
import org.thymeleaf.exceptions.*;

import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Lists.*;

public class TennisStatsErrorAttributes extends DefaultErrorAttributes {

	@Override public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
		errorAttributes.put("inFragment", isInFragment(requestAttributes));
		errorAttributes.put("versions", BaseController.VERSIONS);
		return errorAttributes;
	}

	private boolean isInFragment(RequestAttributes requestAttributes) {
		Throwable error = getError(requestAttributes);
		if (error == null)
			return false;
		return reverse(getCausalChain(error)).stream()
			.filter(th -> th instanceof TemplateProcessingException)
			.map(th -> (TemplateProcessingException)th)
			.map(tEx -> tEx.hasTemplateName() && tEx.getTemplateName().startsWith("fragments/"))
			.findFirst().orElse(false);
	}
}
