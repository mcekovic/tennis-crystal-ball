package org.strangeforest.tcb.stats.controler;

import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeControler {

	@RequestMapping("/")
	public String index() {
		return "index";
	}
}
