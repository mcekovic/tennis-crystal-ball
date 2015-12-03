package org.strangeforest.tcb.stats.controler;

import java.util.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.service.*;

public abstract class BaseController {

	@Autowired private DataService dataService;
	@Autowired private HttpServletRequest request;

	@ModelAttribute("lastDataUpdate")
	public Date getVersion() {
		return dataService.getLastUpdate();
	}

	@ModelAttribute("servletPath")
	public String getServletPath() {
		return request.getServletPath();
	}
}
