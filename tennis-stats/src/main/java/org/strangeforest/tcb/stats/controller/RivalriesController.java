package org.strangeforest.tcb.stats.controller;

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
	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;

	@GetMapping("/headToHead")
	public ModelAndView headToHead(
		@RequestParam(name = "playerId1", required = false) Integer playerId1,
		@RequestParam(name = "name1", required = false) String name1,
		@RequestParam(name = "playerId2", required = false) Integer playerId2,
		@RequestParam(name = "name2", required = false) String name2,
		@RequestParam(name = "tab", required = false) String tab
	) {
		Optional<Player> optionalPlayer1 = playerId1 != null ? playerService.getPlayer(playerId1) : (name1 != null ? playerService.getPlayer(name1) : Optional.empty());
		Optional<Player> optionalPlayer2 = playerId2 != null ? playerService.getPlayer(playerId2) : (name2 != null ? playerService.getPlayer(name2) : Optional.empty());

		ModelMap modelMap = new ModelMap();
		addPlayer(modelMap, playerId1, name1, optionalPlayer1, 1);
		addPlayer(modelMap, playerId2, name2, optionalPlayer2, 2);
		modelMap.addAttribute("tab", tab);
		return new ModelAndView("headToHead", modelMap);
	}

	private static void addPlayer(ModelMap modelMap, Integer playerId, String name, Optional<Player> optionalPlayer, int index) {
		if (optionalPlayer.isPresent())
			modelMap.addAttribute("player" + index, optionalPlayer.get());
		else
			modelMap.addAttribute("playerRef" + index, playerId != null ? playerId : name);
	}

	@GetMapping("/headsToHeads")
	public ModelAndView headsToHeads() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("headsToHeads", modelMap);
	}

	@GetMapping("/headsToHeadsTable")
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

	@GetMapping("/greatestRivalries")
	public ModelAndView greatestRivalries() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("greatestRivalries", modelMap);
	}
}
