package org.strangeforest.tcb.stats.controller;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.records.*;

@Controller
public class RecordsController extends PageController {

	@GetMapping("/records")
	public ModelAndView records() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("recordCount", Records.getRecordCount());
		modelMap.addAttribute("recordCategoryClasses", Records.getRecordCategoryClasses());
		modelMap.addAttribute("infamousRecordCategoryClasses", Records.getInfamousRecordCategoryClasses());
		return new ModelAndView("records", modelMap);
	}

	@GetMapping("/record")
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
