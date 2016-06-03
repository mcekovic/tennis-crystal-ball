package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.records.*;

import static com.google.common.base.Strings.*;

@Controller
public class RecordsController extends PageController {

	@RequestMapping({"/records", "/record"})
	public ModelAndView record(
		@RequestParam(value = "recordId", required = false) String recordId,
		@RequestParam(value = "active", required = false) Boolean active,
		@RequestParam(value = "infamous", required = false) Boolean infamous
	) {
		ModelMap modelMap = new ModelMap();
		if (!isNullOrEmpty(recordId)) {
			Record record = Records.getRecord(recordId);
			modelMap.addAttribute("infamous", record.isInfamous());
			modelMap.addAttribute("record", record);
			modelMap.addAttribute("active", active != null && active);
		}
		else
			modelMap.addAttribute("infamous", infamous != null && infamous);
		modelMap.addAttribute("recordCategories", Records.getRecordCategories());
		modelMap.addAttribute("infamousRecordCategories", Records.getInfamousRecordCategories());
		return new ModelAndView("records", modelMap);
	}
}
