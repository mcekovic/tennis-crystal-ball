package org.strangeforest.tcb.stats.controller;

import java.time.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.strangeforest.tcb.stats.service.*;

public abstract class PageController extends BaseController {

	@Autowired protected DataService dataService;

	@ModelAttribute("dataUpdate")
	public LocalDate getDataUpdate() {
		return dataService.getDataUpdate();
	}

	@ModelAttribute("priceFormats")
	public PriceFormat[] getPriceFormats() {
		return PriceFormat.values();
	}
}
