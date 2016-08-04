package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.service.*;

public abstract class PageController extends BaseController {

	@Autowired private DataService dataService;

	@ModelAttribute("lastDataUpdate")
	public Date getLastDataUpdate() {
		return dataService.getLastUpdate();
	}
}
