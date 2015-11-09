package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.service.*;

public abstract class BaseController {

	@Autowired private DataService dataService;

	@ModelAttribute("lastDataUpdate")
	public Date getVersion() {
		return dataService.getLastUpdate();
	}
}
