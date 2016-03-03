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
public class TournamentController extends BaseController {

	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;
	@Autowired private DataService dateService;

	@RequestMapping("/tournament")
	public ModelAndView tournament(
		@RequestParam(value = "tournamentId") int tournamentId
	) {
		Tournament tournament = tournamentService.getTournament(tournamentId);
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournament", tournament);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournament", modelMap);
	}

	@RequestMapping("/tournamentEvents")
	public ModelAndView tournamentEvents() {
		List<Integer> seasons = dateService.getSeasons();
		List<TournamentItem> tournaments = tournamentService.getTournaments();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("tournaments", tournaments);
		return new ModelAndView("tournamentEvents", modelMap);
	}

	@RequestMapping("/tournamentEventDraw")
	public ModelAndView tournamentEventDraw(
		@RequestParam(value = "tournamentEventId") int tournamentEventId
	) {
		TournamentEvent tournamentEvent = tournamentService.getTournamentEvent(tournamentEventId);
		TournamentEventDraw draw = matchesService.getTournamentEventDraw(tournamentEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEvent", tournamentEvent);
		modelMap.addAttribute("draw", draw);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournamentEventDraw", modelMap);
	}
}
