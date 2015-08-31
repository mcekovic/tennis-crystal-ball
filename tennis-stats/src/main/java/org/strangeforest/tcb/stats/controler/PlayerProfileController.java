package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class PlayerProfileController {

	@Autowired private PlayerService playerService;

	@RequestMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(value = "id", required = false) Integer id,
		@RequestParam(value = "name", required = false) String name
	) {
		if (id == null && name == null)
			return new ModelAndView("playerProfile");

		Player player = id != null ? playerService.getPlayer(id) : playerService.getPlayer(name);
		List<Integer> seasons = playerService.getPlayerSeasons(player.getId());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("seasons", seasons);
		return new ModelAndView("playerProfile", modelMap);
	}
}
