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
public class PlayerMatchesController {

	@Autowired private PlayerService playerService;

	@RequestMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "playerName", required = false) String playerName,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		if (playerId == null && playerName == null)
			return new ModelAndView("playerMatches");

		Player player = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(playerName);
		List<Integer> seasons = playerService.getPlayerSeasons(player.getId());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("seasons", seasons);
		return new ModelAndView("playerMatches", modelMap);
	}
}
