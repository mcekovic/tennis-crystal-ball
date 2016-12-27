package org.strangeforest.tcb.stats.controller;

import java.util.*;

import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

@Controller
public class PlayerProfileController extends PageController {

	@Autowired private PlayerService playerService;
	@Autowired private TournamentService tournamentService;
	@Autowired private RankingsService rankingsService;
	@Autowired private PlayerTimelineService timelineService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private GOATPointsService goatPointsService;

	@GetMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponentId", required = false) Integer opponentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "outcome", required = false) String outcome,
	   HttpServletRequest request
	) {
		if (playerId == null && name == null)
			return new ModelAndView("playerProfile");

		Optional<Player> optionalPlayer = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(name);

		ModelMap modelMap = new ModelMap();
		if (optionalPlayer.isPresent()) {
			Player player = optionalPlayer.get();
			modelMap.addAttribute("player", player);
			modelMap.addAttribute("permalink", playerPermalink(player, request));
		}
		else
			modelMap.addAttribute("playerRef", playerId != null ? playerId : name);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("outcome", outcome);
		return new ModelAndView("playerProfile", modelMap);
	}

	private static String playerPermalink(Player player, HttpServletRequest request) {
		return request.getServletPath() + '?' + request.getQueryString().replaceFirst("playerId=\\d+", "name=" + player.getName());
	}

	@GetMapping("/playerProfileTab")
	public ModelAndView playerProfileTab(
		@RequestParam(name = "playerId") int playerId
	) {
		Player player = playerService.getPlayer(playerId).get();
		PlayerPerformance playerPerf = statisticsService.getPlayerPerformance(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("playerPerf", playerPerf);
		return new ModelAndView("playerProfileTab", modelMap);
	}

	@GetMapping("/playerTournaments")
	public ModelAndView playerTournaments(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "result", required = false) String result
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournaments(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
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

	@GetMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponentId", required = false) Integer opponentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "outcome", required = false) String outcome
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
		if (opponentId != null) {
			modelMap.addAttribute("opponentId", opponentId);
			modelMap.addAttribute("opponentName", playerService.getPlayerName(opponentId));
		}
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("outcome", outcome);
		return new ModelAndView("playerMatches", modelMap);
	}

	@GetMapping("/playerTimeline")
	public ModelAndView playerTimeline(
		@RequestParam(name = "playerId") int playerId
	) {
		PlayerTimeline timeline = timelineService.getPlayerTimeline(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("playerTimeline", modelMap);
	}

	@GetMapping("/playerRivalries")
	public ModelAndView playerRivalries(
		@RequestParam(name = "playerId") int playerId
	) {
		String name = playerService.getPlayerName(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("playerRivalries", modelMap);
	}

	@GetMapping("/playerRankings")
	public ModelAndView playerRankings(
		@RequestParam(name = "playerId") int playerId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("highlights", rankingsService.getRankingHighlights(playerId));
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("rankTypes", RankType.values());
		return new ModelAndView("playerRankings", modelMap);
	}

	@GetMapping("/playerPerformance")
	public ModelAndView playerPerformance(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season
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

	@GetMapping("/playerPerformanceChart")
	public ModelAndView playerPerformanceChart(
		@RequestParam(name = "playerId") int playerId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		return new ModelAndView("playerPerformanceChart", modelMap);
	}

	@GetMapping("/playerStatsTab")
	public ModelAndView playerStatsTab(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, MatchFilter.forStats(season, level, surface));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("stats", stats);
		return new ModelAndView("playerStatsTab", modelMap);
	}

	@GetMapping("/playerStatsChart")
	public ModelAndView playerStatsChart(
		@RequestParam(name = "playerId") int playerId
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

	@GetMapping("/playerGOATPoints")
	public ModelAndView playerGOATPoints(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season
	) {
		Map<String, List<String>> levelResults = goatPointsService.getLevelResults();
		PlayerGOATPoints goatPoints = goatPointsService.getPlayerGOATPoints(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("levelResults", levelResults);
		modelMap.addAttribute("levelResultCount", levelResults.values().stream().mapToInt(List::size).sum());
		modelMap.addAttribute("goatPoints", goatPoints);
		modelMap.addAttribute("highlightSeason", season);
		return new ModelAndView("playerGOATPoints", modelMap);
	}

	@GetMapping("/playerRecords")
	public ModelAndView playerRecords(
		@RequestParam(name = "playerId") int playerId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("recordCategories", Records.getRecordCategories());
		modelMap.addAttribute("infamousRecordCategories", Records.getInfamousRecordCategories());
		return new ModelAndView("playerRecords", modelMap);
	}
}
