package org.strangeforest.tcb.stats.controler;

import java.util.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

public abstract class BaseController {

	@Autowired private HttpServletRequest request;

	public static final Map<String, String> VERSIONS = new HashMap<String, String>() {{
		put("jquery", "2.2.3");
		put("jquery-ui", "1.11.4");
		put("bootstrap", "3.3.6");
		put("bootgrid", "1.3.1");
	}};

	@ModelAttribute("versions")
	public Map<String, String> getVersions() {
		return VERSIONS;
	}

	@ModelAttribute("servletPath")
	public String getServletPath() {
		return request.getServletPath();
	}
}
