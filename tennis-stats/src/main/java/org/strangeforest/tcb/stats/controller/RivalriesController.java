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
import org.strangeforest.tcb.util.*;

import static com.google.common.base.Strings.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class RivalriesController extends PageController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private PlayerService playerService;
	@Autowired private PerformanceService performanceService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private TournamentService tournamentService;
	@Autowired private GOATPointsService goatPointsService;
	@Autowired private MatchPredictionService matchPredictionService;
	@Autowired private RankingsService rankingsService;
	@Autowired private DataService dataService;

	@GetMapping("/headToHead")
	public ModelAndView headToHead(
		@RequestParam(name = "playerId1", required = false) Integer playerId1,
		@RequestParam(name = "name1", required = false) String name1,
		@RequestParam(name = "playerId2", required = false) Integer playerId2,
		@RequestParam(name = "name2", required = false) String name2,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "season", required = false) Integer season,
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
		modelMap.addAttribute("season", season);
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
		PlayerPerformance performance1 = performanceService.getPlayerPerformance(playerId1);
		PlayerPerformance performance2 = performanceService.getPlayerPerformance(playerId2);
		FavoriteSurface favoriteSurface1 = new FavoriteSurface(performance1);
		FavoriteSurface favoriteSurface2 = new FavoriteSurface(performance2);
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("favoriteSurface1", favoriteSurface1);
		modelMap.addAttribute("favoriteSurface2", favoriteSurface2);
		modelMap.addAttribute("performance1", performance1);
		modelMap.addAttribute("performance2", performance2);
		modelMap.addAttribute("stats1", stats1);
		return new ModelAndView("h2hProfiles", modelMap);
	}

	@GetMapping("/h2hMatches")
	public ModelAndView h2hMatches(
      @RequestParam(name = "playerId1") int playerId1,
      @RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "surface", required = false) String surface,
      @RequestParam(name = "round", required = false) String round
   ) {
		Player player1 = playerService.getPlayer(playerId1).get();
		Player player2 = playerService.getPlayer(playerId2).get();
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, level, surface, round));
		List<Integer> seasons = getSeasonsIntersection(playerId1, playerId2);
		List<TournamentItem> tournaments = new ArrayList<>(tournamentService.getPlayerTournaments(playerId1));
		tournaments.retainAll(tournamentService.getPlayerTournaments(playerId2));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("stats1", stats1);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("round", round);
		return new ModelAndView("h2hMatches", modelMap);
	}

	@GetMapping("/h2hSeason")
	public ModelAndView h2hSeason(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season
	) {
		NavigableSet<Integer> seasons = getSeasonsUnion(playerId1, playerId2);
		if (season == null)
			season = !seasons.isEmpty() ? seasons.first() : Integer.valueOf(LocalDate.now().getYear());
		Map<EventResult, List<PlayerTournamentEvent>> seasonHighlights1 = tournamentService.getPlayerSeasonHighlights(playerId1, season, 4);
		Map<EventResult, List<PlayerTournamentEvent>> seasonHighlights2 = tournamentService.getPlayerSeasonHighlights(playerId2, season, 4);
		List<EventResult> eventResults = union(seasonHighlights1.keySet(), seasonHighlights2.keySet()).stream().limit(4).collect(toList());
		PlayerSeasonGOATPoints seasonGOATPoints1 = goatPointsService.getPlayerSeasonGOATPoints(playerId1, season);
		PlayerSeasonGOATPoints seasonGOATPoints2 = goatPointsService.getPlayerSeasonGOATPoints(playerId2, season);
		PlayerPerformanceEx performance1 = performanceService.getPlayerSeasonPerformanceEx(playerId1, season);
		PlayerPerformanceEx performance2 = performanceService.getPlayerSeasonPerformanceEx(playerId2, season);
		Set<Surface> surfaces = union(performance1.getSurfaceMatches().keySet(), performance2.getSurfaceMatches().keySet());
		Set<TournamentLevel> levels = union(performance1.getLevelMatches().keySet(), performance2.getLevelMatches().keySet());
		Set<Opponent> oppositions = union(performance1.getOppositionMatches().keySet(), performance2.getOppositionMatches().keySet());
		Set<Round> rounds = union(performance1.getRoundMatches().keySet(), performance2.getRoundMatches().keySet());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("eventResults", eventResults);
		modelMap.addAttribute("seasonHighlights1", seasonHighlights1);
		modelMap.addAttribute("seasonHighlights2", seasonHighlights2);
		modelMap.addAttribute("seasonGOATPoints1", seasonGOATPoints1);
		modelMap.addAttribute("seasonGOATPoints2", seasonGOATPoints2);
		modelMap.addAttribute("surfaces", surfaces);
		modelMap.addAttribute("levels", levels);
		modelMap.addAttribute("oppositions", oppositions);
		modelMap.addAttribute("rounds", rounds);
		modelMap.addAttribute("performance1", performance1);
		modelMap.addAttribute("performance2", performance2);
		return new ModelAndView("h2hSeason", modelMap);
	}

	@GetMapping("/h2hRankings")
	public ModelAndView h2hRankings(
		@RequestParam(name = "playerId") int[] playerId
	) {
		List<Integer> seasons = playerService.getPlayersSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("rankTypes", RankType.values());
		return new ModelAndView("playerRankings", modelMap);
	}

	@GetMapping("/h2hPerformance")
	public ModelAndView h2hPerformance(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season
	) {
		Set<Integer> seasons = getSeasonsUnion(playerId1, playerId2);
		PlayerPerformance perf1 = season == null ? performanceService.getPlayerPerformance(playerId1) : performanceService.getPlayerSeasonPerformance(playerId1, season);
		PlayerPerformance perf2 = season == null ? performanceService.getPlayerPerformance(playerId2) : performanceService.getPlayerSeasonPerformance(playerId2, season);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("perf1", perf1);
		modelMap.addAttribute("perf2", perf2);
		return new ModelAndView("h2hPerformance", modelMap);
	}

	@GetMapping("/h2hPerformanceChart")
	public ModelAndView h2hPerformanceChart(
		@RequestParam(name = "playerId") int[] playerId
	) {
		List<Integer> seasons = playerService.getPlayersSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		return new ModelAndView("playerPerformanceChart", modelMap);
	}

	@GetMapping("/h2hStats")
	public ModelAndView h2hStats(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface
	) {
		Set<Integer> seasons = getSeasonsUnion(playerId1, playerId2);
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forStats(season, level, surface));
		PlayerStats stats2 = statisticsService.getPlayerStats(playerId2, MatchFilter.forStats(season, level, surface));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("stats1", stats1);
		modelMap.addAttribute("stats2", stats2);
		modelMap.addAttribute("statsFormatUtil", new StatsFormatUtil());
		return new ModelAndView("h2hStats", modelMap);
	}

	@GetMapping("/h2hStatsChart")
	public ModelAndView h2hStatsChart(
		@RequestParam(name = "playerId") int[] playerId
	) {
		List<Integer> seasons = playerService.getPlayersSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("categoryTypes", StatsCategory.getCategoryTypes());
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		return new ModelAndView("playerStatsChart", modelMap);
	}

	@GetMapping("/h2hGOATPoints")
	public ModelAndView h2hGOATPoints(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2
	) {
		PlayerGOATPoints goatPoints1 = goatPointsService.getPlayerGOATPoints(playerId1);
		PlayerGOATPoints goatPoints2 = goatPointsService.getPlayerGOATPoints(playerId2);
		Set<Integer> seasons = new TreeSet<>(reverseOrder());
		seasons.addAll(goatPoints1.getPlayerSeasons());
		seasons.addAll(goatPoints2.getPlayerSeasons());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("goatPoints1", goatPoints1);
		modelMap.addAttribute("goatPoints2", goatPoints2);
		modelMap.addAttribute("seasons", seasons);
		return new ModelAndView("h2hGOATPoints", modelMap);
	}

	@GetMapping("/h2hHypotheticalMatchup")
	public ModelAndView h2hHypotheticalMatchup(
      @RequestParam(name = "playerId1") int playerId1,
      @RequestParam(name = "playerId2") int playerId2,
      @RequestParam(name = "surface", required = false) String surface,
      @RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "round", required = false) String round,
      @RequestParam(name = "date", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date,
      @RequestParam(name = "date1", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date1,
      @RequestParam(name = "date2", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date2,
      @RequestParam(name = "dateSelector1", required = false) String dateSelector1,
      @RequestParam(name = "dateSelector2", required = false) String dateSelector2,
      @RequestParam(name = "priceFormat", required = false) PriceFormat priceFormat
   ) {
		Player player1 = playerService.getPlayer(playerId1).get();
		Player player2 = playerService.getPlayer(playerId2).get();
		Date aDate1 = dateForMatchup(dateSelector1, date1, date, player1);
		Date aDate2 = dateForMatchup(dateSelector2, date2, date, player2);
		MatchPrediction prediction = matchPredictionService.predictMatch(
			playerId1, playerId2, aDate1, aDate2,
			Surface.safeDecode(surface), TournamentLevel.safeDecode(level), Round.safeDecode(round)
      );
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, level, surface, round));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("rounds", Round.ROUNDS);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("date", date != null ? toDate(date) : aDate1);
		modelMap.addAttribute("date1", aDate1);
		modelMap.addAttribute("date2", aDate2);
		modelMap.addAttribute("dateSelector1", dateSelector1);
		modelMap.addAttribute("dateSelector2", dateSelector2);
		modelMap.addAttribute("prediction", prediction);
		modelMap.addAttribute("priceFormats", PriceFormat.values());
		modelMap.addAttribute("priceFormat", priceFormat);
		modelMap.addAttribute("stats1", stats1);
		return new ModelAndView("h2hHypotheticalMatchup", modelMap);
	}

	private Date dateForMatchup(String dateSelector, LocalDate playerDate, LocalDate date, Player player) {
		if (!isNullOrEmpty(dateSelector))
			return selectDate(player, dateSelector);
		else if (playerDate != null)
			return toDate(playerDate);
		else if (date != null)
			return toDate(date);
		else
			return defaultDate(player);
	}

	private Date selectDate(Player player, String dateSelector) {
		int playerId = player.getId();
		switch (dateSelector) {
			case "Today": return toDate(LocalDate.now());
			case "CareerEnd": return playerService.getPlayerCareerEnd(playerId);
			case "PeakRank": return rankingsService.getRankingHighlights(playerId).getBestRankDate();
			case "PeakRankPoints": return rankingsService.getRankingHighlights(playerId).getBestRankPointsDate();
			case "PeakEloRank": return rankingsService.getRankingHighlights(playerId).getBestEloRankDate();
			case "PeakEloRating": return rankingsService.getRankingHighlights(playerId).getBestEloRatingDate();
			case "PeakHardEloRank": return rankingsService.getRankingHighlights(playerId).getBestHardEloRankDate();
			case "PeakHardEloRating": return rankingsService.getRankingHighlights(playerId).getBestHardEloRatingDate();
			case "PeakClayEloRank": return rankingsService.getRankingHighlights(playerId).getBestClayEloRankDate();
			case "PeakClayEloRating": return rankingsService.getRankingHighlights(playerId).getBestClayEloRatingDate();
			case "PeakGrassEloRank": return rankingsService.getRankingHighlights(playerId).getBestGrassEloRankDate();
			case "PeakGrassEloRating": return rankingsService.getRankingHighlights(playerId).getBestGrassEloRatingDate();
			case "PeakCarpetEloRank": return rankingsService.getRankingHighlights(playerId).getBestCarpetEloRankDate();
			case "PeakCarpetEloRating": return rankingsService.getRankingHighlights(playerId).getBestCarpetEloRatingDate();
			default: return defaultDate(player);
		}
	}

	private Date defaultDate(Player player) {
		if (player.isActive())
			return toDate(LocalDate.now());
		else {
			Date careerEndDate = playerService.getPlayerCareerEnd(player.getId());
			return careerEndDate != null ? careerEndDate : toDate(LocalDate.now());
		}
	}

	@GetMapping("/headsToHeads")
	public ModelAndView headsToHeads() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
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
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("greatestRivalries", modelMap);
	}


	// Util

	private List<Integer> getSeasonsIntersection(int playerId1, int playerId2) {
		List<Integer> seasons = new ArrayList<>(playerService.getPlayerSeasons(playerId1));
		seasons.retainAll(playerService.getPlayerSeasons(playerId2));
		return seasons;
	}

	private NavigableSet<Integer> getSeasonsUnion(int playerId1, int playerId2) {
		NavigableSet<Integer> seasons = new TreeSet<>(reverseOrder());
		seasons.addAll(playerService.getPlayerSeasons(playerId1));
		seasons.addAll(playerService.getPlayerSeasons(playerId2));
		return seasons;
	}

	private static <T> Set<T> union(Collection<T> col1, Collection<T> col2) {
		Set<T> union = new TreeSet<>(col1);
		union.addAll(col2);
		return union;
	}
}
