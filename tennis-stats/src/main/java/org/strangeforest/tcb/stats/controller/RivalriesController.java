package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class RivalriesController extends PageController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private TournamentService tournamentService;
	@Autowired private MatchPredictionService matchPredictionService;
	@Autowired private RankingsService rankingsService;

	@GetMapping("/headToHead")
	public ModelAndView headToHead(
		@RequestParam(name = "playerId1", required = false) Integer playerId1,
		@RequestParam(name = "name1", required = false) String name1,
		@RequestParam(name = "playerId2", required = false) Integer playerId2,
		@RequestParam(name = "name2", required = false) String name2,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round
	) {
		Optional<Player> optionalPlayer1 = playerId1 != null ? playerService.getPlayer(playerId1) : (name1 != null ? playerService.getPlayer(name1) : Optional.empty());
		Optional<Player> optionalPlayer2 = playerId2 != null ? playerService.getPlayer(playerId2) : (name2 != null ? playerService.getPlayer(name2) : Optional.empty());

		ModelMap modelMap = new ModelMap();
		addPlayer(modelMap, playerId1, name1, optionalPlayer1, 1);
		addPlayer(modelMap, playerId2, name2, optionalPlayer2, 2);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("round", round);
		return new ModelAndView("headToHead", modelMap);
	}

	private static void addPlayer(ModelMap modelMap, Integer playerId, String name, Optional<Player> optionalPlayer, int index) {
		if (optionalPlayer.isPresent())
			modelMap.addAttribute("player" + index, optionalPlayer.get());
		else
			modelMap.addAttribute("playerRef" + index, playerId != null ? playerId : name);
	}

	@GetMapping("/h2hProfiles")
	public ModelAndView h2hProfiles(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2
	) {
		Player player1 = playerService.getPlayer(playerId1).get();
		Player player2 = playerService.getPlayer(playerId2).get();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		return new ModelAndView("h2hProfiles", modelMap);
	}

	@GetMapping("/h2hMatches")
	public ModelAndView h2hMatches(
      @RequestParam(name = "playerId1") int playerId1,
      @RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "surface", required = false) String surface,
      @RequestParam(name = "round", required = false) String round
   ) {
		String name1 = playerService.getPlayerName(playerId1);
		String name2 = playerService.getPlayerName(playerId2);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId1);
		seasons.retainAll(playerService.getPlayerSeasons(playerId2));
		List<TournamentItem> tournaments = tournamentService.getPlayerTournaments(playerId1);
		tournaments.retainAll(tournamentService.getPlayerTournaments(playerId2));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerName1", name1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("playerName2", name2);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("round", round);
		return new ModelAndView("h2hMatches", modelMap);
	}

	@GetMapping("/h2hPerformance")
	public ModelAndView h2hPerformance(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season
	) {
		Set<Integer> seasons = new TreeSet<>(reverseOrder());
		seasons.addAll(playerService.getPlayerSeasons(playerId1));
		seasons.addAll(playerService.getPlayerSeasons(playerId2));
		PlayerPerformance perf1 = season == null ? statisticsService.getPlayerPerformance(playerId1) : statisticsService.getPlayerSeasonPerformance(playerId1, season);
		PlayerPerformance perf2 = season == null ? statisticsService.getPlayerPerformance(playerId2) : statisticsService.getPlayerSeasonPerformance(playerId2, season);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("perf1", perf1);
		modelMap.addAttribute("perf2", perf2);
		return new ModelAndView("h2hPerformance", modelMap);
	}

	@GetMapping("/h2hHypotheticalMatchup")
	public ModelAndView h2hHypotheticalMatchup(
      @RequestParam(name = "playerId1") int playerId1,
      @RequestParam(name = "playerId2") int playerId2,
      @RequestParam(name = "date", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date,
      @RequestParam(name = "date1", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date1,
      @RequestParam(name = "date2", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date2,
      @RequestParam(name = "surface", required = false) String surface,
      @RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "round", required = false) String round
   ) {
		Player player1 = playerService.getPlayer(playerId1).get();
		Player player2 = playerService.getPlayer(playerId2).get();
		LocalDate today = LocalDate.now();
		if (date == null && date1 == null && date2 == null)
			date = today;
		Date aDate1 = toDate(date1 != null ? date1 : date);
		Date aDate2 = toDate(date2 != null ? date2 : date);
		MatchPrediction prediction = matchPredictionService.predictMatch(
			playerId1, playerId2, aDate1, aDate2,
			Surface.safeDecode(surface), TournamentLevel.safeDecode(level), Round.safeDecode(round)
      );
		RankingHighlights rankingHighlights1 = rankingsService.getRankingHighlights(playerId1);
		RankingHighlights rankingHighlights2 = rankingsService.getRankingHighlights(playerId2);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("rounds", Round.ROUNDS);
		modelMap.addAttribute("date", date != null ? toDate(date) : aDate1);
		modelMap.addAttribute("date1", aDate1);
		modelMap.addAttribute("date2", aDate2);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("prediction", prediction);
		modelMap.addAttribute("rankingHighlights1", rankingHighlights1);
		modelMap.addAttribute("rankingHighlights2", rankingHighlights2);
		modelMap.addAttribute("today", toDate(today));
		return new ModelAndView("h2hHypotheticalMatchup", modelMap);
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
