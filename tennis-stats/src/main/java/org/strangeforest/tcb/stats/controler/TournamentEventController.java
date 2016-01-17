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
public class TournamentEventController extends BaseController {

	@Autowired private TournamentService tournamentService;
	@Autowired private DataService dateService;

	@RequestMapping("/tournamentEvents")
	public ModelAndView tournamentEvents() {
		List<Integer> seasons = dateService.getSeasons();
		List<TournamentItem> tournaments = tournamentService.getTournaments();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", Options.TOURNAMENT_LEVELS_W_O_D_C);
		modelMap.addAttribute("surfaces", Options.SURFACES);
		modelMap.addAttribute("tournaments", tournaments);
		return new ModelAndView("tournamentEvents", modelMap);
	}
}
