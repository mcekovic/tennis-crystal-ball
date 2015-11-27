package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static java.util.stream.Collectors.*;

@Controller
public class RivalriesController extends BaseController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerService playerService;

	@RequestMapping("/greatestRivalries")
	public ModelAndView greatestRivalries() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("levels", Options.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Options.SURFACES);
		return new ModelAndView("greatestRivalries", modelMap);
	}

	@RequestMapping("/headsToHeads")
	public ModelAndView headsToHeads() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", statisticsService.getSeasons());
		modelMap.addAttribute("levels", Options.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Options.SURFACES);
		return new ModelAndView("headsToHeads", modelMap);
	}

	@RequestMapping("/headsToHeadsTable")
	public ModelAndView headsToHeadsTable(
		@RequestParam(value = "players") String playersCSV,
		@RequestParam(value = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(value = "toSeason", required = false) Integer toSeason,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface
	) {
		List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
		RivalryFilter filter = new RivalryFilter(RangeUtil.toRange(fromSeason, toSeason), level, surface);

		List<Integer> playerIds = playerService.findPlayerIds(players);
		HeadsToHeads headsToHeads = rivalriesService.getHeadsToHeads(playerIds, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("headsToHeads", headsToHeads);
		return new ModelAndView("headsToHeadsTable", modelMap);
	}
}
