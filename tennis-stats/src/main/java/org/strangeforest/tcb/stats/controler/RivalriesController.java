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
public class RivalriesController extends PageController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private DataService dataService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerService playerService;

	@RequestMapping("/greatestRivalries")
	public ModelAndView greatestRivalries() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("greatestRivalries", modelMap);
	}

	@RequestMapping("/headsToHeads")
	public ModelAndView headsToHeads() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("headsToHeads", modelMap);
	}

	@RequestMapping("/headsToHeadsTable")
	public ModelAndView headsToHeadsTable(
		@RequestParam(name = "players") String playersCSV,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "statsVsAll") boolean statsVsAll
	) {
		List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
		RivalryFilter filter = new RivalryFilter(RangeUtil.toRange(fromSeason, toSeason), level, surface, round);

		List<Integer> playerIds = playerService.findPlayerIds(players);
		HeadsToHeads headsToHeads = rivalriesService.getHeadsToHeads(playerIds, filter);
		Map<Integer, PlayerStats> playersStats = statisticsService.getPlayersStats(playerIds, filter, statsVsAll);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("headsToHeads", headsToHeads);
		modelMap.addAttribute("playersStats", playersStats);
		return new ModelAndView("headsToHeadsTable", modelMap);
	}
}
