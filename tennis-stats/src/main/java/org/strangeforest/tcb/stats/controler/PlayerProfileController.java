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
public class PlayerProfileController extends BaseController {

	@Autowired private PlayerService playerService;
	@Autowired private TournamentService tournamentService;
	@Autowired private PlayerTimelineService timelineService;
	@Autowired private StatisticsService statisticsService;

	@RequestMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "name", required = false) String name,
		@RequestParam(value = "season", required = false) Integer season
	) {
		if (playerId == null && name == null)
			return new ModelAndView("playerProfile");

		Player player = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(name);
		List<Integer> seasons = playerService.getPlayerSeasons(player.getId());
		List<Tournament> tournaments = tournamentService.getPlayerTournaments(player.getId());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("season", season);
		return new ModelAndView("playerProfile", modelMap);
	}

	@RequestMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<Tournament> tournaments = tournamentService.getPlayerTournaments(playerId);
		List<TournamentEvent> tournamentEvents = tournamentService.getPlayerTournamentEvents(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
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

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("playerTimeline", modelMap);
	}

	@RequestMapping("/playerRivalries")
	public ModelAndView playerRivalries(
		@RequestParam(value = "playerId") int playerId
	) {
		return new ModelAndView("playerRivalries", "playerId", playerId);
	}

	@RequestMapping("/playerRankingChart")
	public ModelAndView playerRankingChart(
		@RequestParam(value = "playerId") int playerId
	) {
		return new ModelAndView("playerRankingChart", "playerId", playerId);
	}

	@RequestMapping("/playerPerformance")
	public ModelAndView playerPerformance(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		PlayerPerformance perf = season == null
			? statisticsService.getPlayerPerformance(playerId)
			: statisticsService.getPlayerSeasonPerformance(playerId, season);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("perf", perf);
		return new ModelAndView("playerPerformance", modelMap);
	}

	@RequestMapping("/playerStatsTab")
	public ModelAndView playerStatsTab(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "surface", required = false) String surface
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		PlayerStats stats;
		if (season != null) {
			stats = surface != null
				? statisticsService.getPlayerSeasonSurfaceStats(playerId, season, surface)
				: statisticsService.getPlayerSeasonStats(playerId, season);
		}
		else {
			stats = surface != null
				? statisticsService.getPlayerSurfaceStats(playerId, surface)
				: statisticsService.getPlayerStats(playerId);
		}

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("playerStatsTab", modelMap);
	}
}
