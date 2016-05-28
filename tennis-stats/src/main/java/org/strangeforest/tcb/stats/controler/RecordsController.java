package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.records.*;

@Controller
public class RecordsController extends PageController {

	@RequestMapping("/records")
	public ModelAndView records() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("recordCategories", Records.getRecordCategories());
		return new ModelAndView("records", modelMap);
	}
}
