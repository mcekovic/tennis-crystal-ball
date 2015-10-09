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
	@Autowired private TournamentService tournamentService;
	@Autowired private PlayerTimelineService timelineService;

	@RequestMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "name", required = false) String name
	) {
		if (playerId == null && name == null)
			return new ModelAndView("playerProfile");

		Player player = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(name);
		List<Integer> seasons = playerService.getPlayerSeasons(player.getId());
		List<Tournament> tournaments = tournamentService.getPlayerTournaments(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("tournaments", tournaments);
		return new ModelAndView("playerProfile", modelMap);
	}

	@RequestMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<Tournament> tournaments = tournamentService.getPlayerTournaments(playerId);
		List<TournamentEvent> tournamentEvents = tournamentService.getPlayerTournamentEvents(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("tournamentEvents", tournamentEvents);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		return new ModelAndView("playerMatches", modelMap);
	}

	@RequestMapping("/playerTimeline")
	public ModelAndView playerTimeline(
		@RequestParam(value = "playerId") int playerId
	) {
		PlayerTimeline timeline = timelineService.getPlayerTimeline(playerId);
		return new ModelAndView("playerTimeline", "timeline", timeline);
	}
}
