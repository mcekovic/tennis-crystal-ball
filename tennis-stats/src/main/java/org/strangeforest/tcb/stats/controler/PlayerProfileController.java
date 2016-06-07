package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

@Controller
public class PlayerProfileController extends PageController {

	@Autowired private PlayerService playerService;
	@Autowired private TournamentService tournamentService;
	@Autowired private RankingsService rankingsService;
	@Autowired private DataService dataService;
	@Autowired private PlayerTimelineService timelineService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private GOATPointsService goatPointsService;

	@RequestMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(value = "playerId", required = false) Integer playerId,
		@RequestParam(value = "name", required = false) String name,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "result", required = false) String result,
		@RequestParam(value = "round", required = false) String round,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "opponentId", required = false) Integer opponentId
	) {
		if (playerId == null && name == null)
			return new ModelAndView("playerProfile");

		Optional<Player> player = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(name);

		ModelMap modelMap = new ModelMap();
		if (player.isPresent())
			modelMap.addAttribute("player", player.get());
		else
			modelMap.addAttribute("playerRef", playerId != null ? playerId : name);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("opponentId", opponentId);
		return new ModelAndView("playerProfile", modelMap);
	}

	@RequestMapping("/playerTournaments")
	public ModelAndView playerTournaments(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "result", required = false) String result
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournaments(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("result", result);
		return new ModelAndView("playerTournaments", modelMap);
	}

	@RequestMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "round", required = false) String round,
		@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(value = "opponentId", required = false) Integer opponentId
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournaments(playerId);
		List<TournamentEventItem> tournamentEvents = tournamentService.getPlayerTournamentEvents(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("tournamentEvents", tournamentEvents);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		if (opponentId != null) {
			modelMap.addAttribute("opponentId", opponentId);
			modelMap.addAttribute("opponentName", playerService.getPlayerName(opponentId));
		}
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

	@RequestMapping("/playerRankings")
	public ModelAndView playerRankings(
		@RequestParam(value = "playerId") int playerId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("highlights", rankingsService.getRankingHighlights(playerId));
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("playerRankings", modelMap);
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

	@RequestMapping("/playerPerformanceChart")
	public ModelAndView playerPerformanceChart(
		@RequestParam(value = "playerId") int playerId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		return new ModelAndView("playerPerformanceChart", modelMap);
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
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("playerStatsTab", modelMap);
	}

	@RequestMapping("/playerStatsChart")
	public ModelAndView playerStatsChart(
		@RequestParam(value = "playerId") int playerId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("categoryTypes",  StatsCategory.getCategories().entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getType().name())));
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		return new ModelAndView("playerStatsChart", modelMap);
	}

	@RequestMapping("/playerGOATPoints")
	public ModelAndView playerGOATPoints(
		@RequestParam(value = "playerId") int playerId
	) {
		Map<String, List<String>> levelResults = goatPointsService.getLevelResults();
		PlayerGOATPoints goatPoints = goatPointsService.getPlayerGOATPoints(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("levelResults", levelResults);
		modelMap.addAttribute("levelResultCount", levelResults.values().stream().mapToInt(List::size).sum());
		modelMap.addAttribute("goatPoints", goatPoints);
		return new ModelAndView("playerGOATPoints", modelMap);
	}
}
