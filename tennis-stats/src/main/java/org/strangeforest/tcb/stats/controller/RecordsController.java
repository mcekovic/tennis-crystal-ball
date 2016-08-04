package org.strangeforest.tcb.stats.controller;

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
		modelMap.addAttribute("recordCount", Records.getRecordCount());
		modelMap.addAttribute("recordCategories", Records.getRecordCategories());
		modelMap.addAttribute("infamousRecordCategories", Records.getInfamousRecordCategories());
		return new ModelAndView("records", modelMap);
	}

	@RequestMapping("/record")
	public ModelAndView record(
		@RequestParam(name = "recordId") String recordId,
		@RequestParam(name = "active", required = false, defaultValue = "false") boolean active
	) {
		ModelMap modelMap = new ModelMap();
		Record record = Records.getRecord(recordId);
		modelMap.addAttribute("record", record);
		modelMap.addAttribute("active", active);
		return new ModelAndView("record", modelMap);
	}
}
