package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class StatsController {

	@Autowired private StatsService statsService;

	@RequestMapping("/playerStatsTab")
	public ModelAndView playerStatsTab(
		@RequestParam(value = "playerId") int playerId
	) {
		PlayerStats playerStats = statsService.getPlayerStats(playerId);
		return new ModelAndView("playerStatsTab", "stats", playerStats);
	}

	@RequestMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(value = "matchId") long matchId
	) {
		MatchStats matchStats = statsService.getMatchStats(matchId);
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("matchStats", matchStats);
		return new ModelAndView("matchStats", modelMap);
	}

	@RequestMapping("/playerStats")
	public ModelAndView playerStats(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "searchPhrase", required = false) String searchPhrase
	) {
		TournamentEventFilter filter = new TournamentEventFilter(season, level, surface, tournamentId, tournamentEventId, searchPhrase);
		PlayerStats playerStats = statsService.getPlayerStats(playerId, filter);
		return new ModelAndView("playerStats", "stats", playerStats);
	}
}
