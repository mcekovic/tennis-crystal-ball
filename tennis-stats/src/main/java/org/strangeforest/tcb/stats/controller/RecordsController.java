package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;

@Controller
public class RecordsController extends PageController {

	@Autowired private GOATLegendService goatLegendService;

	@GetMapping("/records")
	public ModelAndView records(
		@RequestParam(name = "category", required = false) String category
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("recordCount", Records.getRecordCount());
		modelMap.addAttribute("recordCategoryClasses", Records.getRecordCategoryClasses());
		modelMap.addAttribute("infamousRecordCategoryClasses", Records.getInfamousRecordCategoryClasses());
		modelMap.addAttribute("category", category);
		if (!isNullOrEmpty(category))
			modelMap.addAttribute("infamous", Records.isInfampus(category));
		return new ModelAndView("records", modelMap);
	}

	@GetMapping("/record")
	public ModelAndView record(
		@RequestParam(name = "recordId") String recordId,
		@RequestParam(name = "active", required = false, defaultValue = F) boolean active
	) {
		var goatPoints = goatLegendService.getRecordGOATPoints(recordId);

		var modelMap = new ModelMap();
		var record = Records.getRecord(recordId);
		modelMap.addAttribute("record", record);
		modelMap.addAttribute("active", active);
		modelMap.addAttribute("goatPoints", goatPoints);
		return new ModelAndView("record", modelMap);
	}
}
