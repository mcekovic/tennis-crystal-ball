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
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentEvent> tournamentEvents = playerService.getPlayerTournamentEvents(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("tournamentEvents", tournamentEvents);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		return new ModelAndView("playerMatches", modelMap);
	}
}
