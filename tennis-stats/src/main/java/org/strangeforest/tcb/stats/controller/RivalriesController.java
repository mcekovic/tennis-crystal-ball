package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;
import java.util.stream.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;
import com.neovisionaries.i18n.*;

import static com.google.common.base.Strings.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class RivalriesController extends PageController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private PlayerService playerService;
	@Autowired private PerformanceService performanceService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;
	@Autowired private GOATPointsService goatPointsService;
	@Autowired private GOATLegendService goatLegendService;
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
		@RequestParam(name = "season1", required = false) Integer season1,
		@RequestParam(name = "season2", required = false) Integer season2,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "rankType", required = false) String rankType
	) {
		Player player1 = playerId1 != null ? playerService.getPlayer(playerId1) : (name1 != null ? playerService.getPlayer(name1) : null);
		Player player2 = playerId2 != null ? playerService.getPlayer(playerId2) : (name2 != null ? playerService.getPlayer(name2) : null);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("season1", season1);
		modelMap.addAttribute("season2", season2);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("params", ParamsUtil.INSTANCE);
		return new ModelAndView("headToHead", modelMap);
	}

	@GetMapping("/h2hProfiles")
	public ModelAndView h2hProfiles(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2
	) {
		Player player1 = playerService.getPlayer(playerId1);
		Player player2 = playerService.getPlayer(playerId2);
		PlayerPerformance performance1 = performanceService.getPlayerPerformance(playerId1);
		PlayerPerformance performance2 = performanceService.getPlayerPerformance(playerId2);
		FavoriteSurface favoriteSurface1 = new FavoriteSurface(performance1);
		FavoriteSurface favoriteSurface2 = new FavoriteSurface(performance2);
		int seasonCount1 = playerService.getPlayerSeasons(playerId1).size();
		int seasonCount2 = playerService.getPlayerSeasons(playerId2).size();
		Integer bestSeason1 = playerService.getPlayerBestSeason(playerId1);
		Integer bestSeason2 = playerService.getPlayerBestSeason(playerId2);
		BootgridTable<PlayerTournamentEvent> lastEvent1 = tournamentService.getPlayerTournamentEventsTable(playerId1, TournamentEventResultFilter.EMPTY, "date DESC", 1, 1);
		BootgridTable<PlayerTournamentEvent> lastEvent2 = tournamentService.getPlayerTournamentEventsTable(playerId2, TournamentEventResultFilter.EMPTY, "date DESC", 1, 1);
		Map<String, Integer> surfaceTitles1 = performanceService.getPlayerSurfaceTitles(playerId1);
		Map<String, Integer> surfaceTitles2 = performanceService.getPlayerSurfaceTitles(playerId2);
		WonDrawLost playerH2H1 = rivalriesService.getPlayerH2H(playerId1).orElse(null);
		WonDrawLost playerH2H2 = rivalriesService.getPlayerH2H(playerId2).orElse(null);

		List<Match> matches1 = matchesService.getPlayerMatchesTable(playerId1, MatchFilter.forOpponent(playerId2, OutcomeFilter.PLAYED), false, "match_id", 1000, 1).getRows();
		PlayerPerformance perf1 = performanceService.getPlayerPerformance(playerId1, PerfStatsFilter.forOpponent(playerId2));
		H2H h2h = new H2H(playerId1, playerId2, matches1);
		H2H surfaceAdjH2H = AdjustedH2H.surfaceAdjustedH2H(perf1, performance1, performance2);
		H2H importanceAdjH2H = AdjustedH2H.importanceAdjustedH2H(playerId1, playerId2, perf1, matches1, goatLegendService.getBigWinMatchFactors());
		H2H adjustedH2H = surfaceAdjH2H.add(importanceAdjH2H).scale(0.5);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("favoriteSurface1", favoriteSurface1);
		modelMap.addAttribute("favoriteSurface2", favoriteSurface2);
		modelMap.addAttribute("seasonCount1", seasonCount1);
		modelMap.addAttribute("seasonCount2", seasonCount2);
		modelMap.addAttribute("bestSeason1", bestSeason1);
		modelMap.addAttribute("bestSeason2", bestSeason2);
		if (lastEvent1.getTotal() > 0)
			modelMap.addAttribute("lastEvent1", lastEvent1.getRows().get(0));
		if (lastEvent2.getTotal() > 0)
			modelMap.addAttribute("lastEvent2", lastEvent2.getRows().get(0));
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		modelMap.addAttribute("performance1", performance1);
		modelMap.addAttribute("performance2", performance2);
		modelMap.addAttribute("surfaceTitles1", surfaceTitles1);
		modelMap.addAttribute("surfaceTitles2", surfaceTitles2);
		modelMap.addAttribute("playerH2H1", playerH2H1);
		modelMap.addAttribute("playerH2H2", playerH2H2);
		modelMap.addAttribute("h2h", h2h);
		modelMap.addAttribute("adjustedH2H", adjustedH2H);
		modelMap.addAttribute("surfaceAdjH2H", surfaceAdjH2H);
		modelMap.addAttribute("importanceAdjH2H", importanceAdjH2H);
		return new ModelAndView("h2hProfiles", modelMap);
	}

	@GetMapping("/h2hSeason")
	public ModelAndView h2hSeason(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "season1", required = false) Integer season1,
		@RequestParam(name = "season2", required = false) Integer season2
	) {
		SortedSet<Integer> seasons = getSeasonsUnion(playerId1, playerId2);
		int defaultSeason = !seasons.isEmpty() ? seasons.first() : Integer.valueOf(LocalDate.now().getYear());
		int defaultSeasons = season != null && season >= 0 ? season : defaultSeason;
		if (season == null)
			season = season1 == null && season2 == null ? defaultSeason : -1;
		if (season1 == null)
			season1 = defaultSeasons;
		if (season2 == null)
			season2 = defaultSeasons;
		Map<EventResult, List<PlayerTournamentEvent>> seasonHighlights1 = tournamentService.getPlayerSeasonHighlights(playerId1, season1, 4);
		Map<EventResult, List<PlayerTournamentEvent>> seasonHighlights2 = tournamentService.getPlayerSeasonHighlights(playerId2, season2, 4);
		List<EventResult> eventResults = union(seasonHighlights1.keySet(), seasonHighlights2.keySet()).stream().limit(4).collect(toList());
		PlayerPerformanceEx seasonPerf1 = performanceService.getPlayerPerformanceEx(playerId1, PerfStatsFilter.forSeason(season1));
		PlayerPerformanceEx seasonPerf2 = performanceService.getPlayerPerformanceEx(playerId2, PerfStatsFilter.forSeason(season2));
		Set<Surface> surfaces = union(seasonPerf1.getSurfaceMatches().keySet(), seasonPerf2.getSurfaceMatches().keySet());
		Set<TournamentLevel> levels = union(seasonPerf1.getLevelMatches().keySet(), seasonPerf2.getLevelMatches().keySet());
		Set<Opponent> oppositions = union(seasonPerf1.getOppositionMatches().keySet(), seasonPerf2.getOppositionMatches().keySet());
		Set<Round> rounds = union(seasonPerf1.getRoundMatches().keySet(), seasonPerf2.getRoundMatches().keySet());
		PlayerSeasonGOATPoints seasonGOATPoints1 = goatPointsService.getPlayerSeasonGOATPoints(playerId1, season1);
		PlayerSeasonGOATPoints seasonGOATPoints2 = goatPointsService.getPlayerSeasonGOATPoints(playerId2, season2);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("season1", season1);
		modelMap.addAttribute("season2", season2);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("eventResults", eventResults);
		modelMap.addAttribute("seasonHighlights1", seasonHighlights1);
		modelMap.addAttribute("seasonHighlights2", seasonHighlights2);
		modelMap.addAttribute("seasonPerf1", seasonPerf1);
		modelMap.addAttribute("seasonPerf2", seasonPerf2);
		modelMap.addAttribute("surfaces", surfaces);
		modelMap.addAttribute("levels", levels);
		modelMap.addAttribute("rounds", rounds);
		modelMap.addAttribute("oppositions", oppositions);
		modelMap.addAttribute("seasonGOATPoints1", seasonGOATPoints1);
		modelMap.addAttribute("seasonGOATPoints2", seasonGOATPoints2);
		return new ModelAndView("h2hSeason", modelMap);
	}

	@GetMapping("/h2hMatches")
	public ModelAndView h2hMatches(
      @RequestParam(name = "playerId1") int playerId1,
      @RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
      @RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
      @RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "bestOf", required = false) Integer bestOf,
      @RequestParam(name = "surface", required = false) String surface,
      @RequestParam(name = "indoor", required = false) Boolean indoor,
      @RequestParam(name = "speed", required = false) String speed,
      @RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
   ) {
		Player player1 = playerService.getPlayer(playerId1);
		Player player2 = playerService.getPlayer(playerId2);
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, season, dateRange, level, bestOf, surface, indoor, speedRange, round, tournamentId, null, null));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("stats1", stats1);
		modelMap.addAttribute("seasons", getSeasonsIntersection(playerId1, playerId2));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournamentService.getPlayersTournamentsIntersection(playerId1, playerId2));
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentId", tournamentId);
		return new ModelAndView("h2hMatches", modelMap);
	}

	@GetMapping("/h2hRankings")
	public ModelAndView h2hRankings(
		@RequestParam(name = "playerId") int[] playerId,
		@RequestParam(name = "rankType", required = false) String rankType
	) {
		List<Integer> seasons = playerService.getPlayersSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("rankCategories", RankCategory.values());
		return new ModelAndView("playerRankings", modelMap);
	}

	@GetMapping("/h2hPerformance")
	public ModelAndView h2hPerformance(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "h2h", defaultValue = F) boolean h2h,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "advFilter", defaultValue = F) boolean advFilter,
		@RequestParam(name = "rawData", defaultValue = F) boolean rawData
	) {
		List<String> countryIds = matchesService.getSameCountryIds(countryId);
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		OpponentFilter opponentFilter1 = h2h ? OpponentFilter.forStats(playerId2) : OpponentFilter.forStats(opponent, countryIds);
		OpponentFilter opponentFilter2 = h2h ? OpponentFilter.forStats(playerId1) : OpponentFilter.forStats(opponent, countryIds);
		PlayerPerformanceEx perf1 = performanceService.getPlayerPerformanceEx(playerId1, new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter1));
		PlayerPerformanceEx perf2 = performanceService.getPlayerPerformanceEx(playerId2, new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter2));
		Set<Surface> perfSurfaces = union(perf1.getSurfaceMatches().keySet(), perf2.getSurfaceMatches().keySet());
		Set<Boolean> perfIndoors = union(perf1.getOutdoorIndoorMatches().keySet(), perf2.getOutdoorIndoorMatches().keySet());
		Set<CourtSpeed> perfSpeeds = union(perf1.getSpeedMatches().keySet(), perf2.getSpeedMatches().keySet());
		Set<TournamentLevel> perfLevels = union(perf1.getLevelMatches().keySet(), perf2.getLevelMatches().keySet());
		Set<Integer> perfBestOfs = union(perf1.getBestOfMatches().keySet(), perf2.getBestOfMatches().keySet());
		Set<Opponent> perfOppositions = union(perf1.getOppositionMatches().keySet(), perf2.getOppositionMatches().keySet());
		Set<PerfMatchScore> perfScores = union(perf1.getScoreCounts().keySet(), perf2.getScoreCounts().keySet());
		Set<Round> perfRounds = union(perf1.getRoundMatches().keySet(), perf2.getRoundMatches().keySet());
		Set<EventResult> perfResults = union(perf1.getResultCounts().keySet(), perf2.getResultCounts().keySet());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("seasons", getSeasonsUnion(playerId1, playerId2));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournamentService.getPlayersTournamentsUnion(playerId1, playerId2));
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", getOpponentCountriesUnion(playerId1, playerId2));
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("h2h", h2h);
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("advFilter", advFilter);
		modelMap.addAttribute("rawData", rawData);
		modelMap.addAttribute("perf1", perf1);
		modelMap.addAttribute("perf2", perf2);
		modelMap.addAttribute("perfSurfaces", perfSurfaces);
		modelMap.addAttribute("perfIndoors", perfIndoors);
		modelMap.addAttribute("perfSpeeds", perfSpeeds);
		modelMap.addAttribute("perfLevels", perfLevels);
		modelMap.addAttribute("perfBestOfs", perfBestOfs);
		modelMap.addAttribute("perfOppositions", perfOppositions);
		modelMap.addAttribute("perfScores", perfScores);
		modelMap.addAttribute("perfRounds", perfRounds);
		modelMap.addAttribute("perfResults", perfResults);
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
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "h2h", defaultValue = F) boolean h2h,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "advFilter", defaultValue = F) boolean advFilter,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "rawData", defaultValue = F) boolean rawData,
		@RequestParam(name = "compare", defaultValue = F) boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		List<String> countryIds = matchesService.getSameCountryIds(countryId);
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		OpponentFilter opponentFilter1 = h2h ? OpponentFilter.forStats(playerId2) : OpponentFilter.forStats(opponent, countryIds);
		OpponentFilter opponentFilter2 = h2h ? OpponentFilter.forStats(playerId1) : OpponentFilter.forStats(opponent, countryIds);
		PlayerStats stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter1));
		PlayerStats stats2 = statisticsService.getPlayerStats(playerId2, MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter2));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("seasons", getSeasonsUnion(playerId1, playerId2));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournamentService.getPlayersTournamentsUnion(playerId1, playerId2));
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", getOpponentCountriesUnion(playerId1, playerId2));
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("h2h", h2h);
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("advFilter", advFilter);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("rawData", rawData);
		modelMap.addAttribute("stats1", stats1);
		modelMap.addAttribute("stats2", stats2);
		modelMap.addAttribute("compare", compare);
		if (compare) {
			MatchFilter compareFilter1 = MatchFilter.forStats(compareSeason, compareLevel, compareSurface, h2h ? OpponentFilter.forStats(playerId2) : null);
			MatchFilter compareFilter2 = MatchFilter.forStats(compareSeason, compareLevel, compareSurface, h2h ? OpponentFilter.forStats(playerId1) : null);
			PlayerStats compareStats1 = statisticsService.getPlayerStats(playerId1, compareFilter1);
			PlayerStats compareStats2 = statisticsService.getPlayerStats(playerId2, compareFilter2);
			if (!compareStats1.isEmpty())
				modelMap.addAttribute("compareStats1", compareStats1);
			if (!compareStats2.isEmpty())
				modelMap.addAttribute("compareStats2", compareStats2);
			modelMap.addAttribute("compareSeason", compareSeason);
			modelMap.addAttribute("compareLevel", compareLevel);
			modelMap.addAttribute("compareSurface", compareSurface);
			modelMap.addAttribute("relativeTo", relativeTo(compareSeason, compareLevel, compareSurface));
		}
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
		@RequestParam(name = "playerId2") int playerId2,
		@RequestParam(name = "surface", required = false) String surface
	) {
		Surface aSurface = Surface.safeDecode(surface);
		PlayerGOATPoints goatPoints1 = goatPointsService.getPlayerGOATPoints(playerId1, aSurface, false);
		PlayerGOATPoints goatPoints2 = goatPointsService.getPlayerGOATPoints(playerId2, aSurface, false);
		Set<Integer> seasons = new TreeSet<>(reverseOrder());
		seasons.addAll(goatPoints1.getPlayerSeasons());
		seasons.addAll(goatPoints2.getPlayerSeasons());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId1", playerId1);
		modelMap.addAttribute("playerId2", playerId2);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surface", surface);
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
      @RequestParam(name = "indoor", required = false) Boolean indoor,
      @RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "round", required = false) String round,
      @RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
      @RequestParam(name = "date1", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date1,
      @RequestParam(name = "date2", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date2,
      @RequestParam(name = "dateSelector1", required = false) String dateSelector1,
      @RequestParam(name = "dateSelector2", required = false) String dateSelector2,
      @CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat,
		@RequestParam(name = "showDetails", defaultValue = F) boolean showDetails,
      @RequestParam(name = "sets1", defaultValue = "0") int sets1,
      @RequestParam(name = "sets2", defaultValue = "0") int sets2,
      @RequestParam(name = "games1", defaultValue = "0") int games1,
      @RequestParam(name = "games2", defaultValue = "0") int games2,
      @RequestParam(name = "points1", defaultValue = "0") int points1,
      @RequestParam(name = "points2", defaultValue = "0") int points2,
      @RequestParam(name = "serve", defaultValue = "1") int serve,
      @RequestParam(name = "command", required = false) String command,
		@RequestParam(name = "inMatch", defaultValue = F) boolean inMatch,
      HttpServletRequest httpRequest
   ) {
		rejectRobots(httpRequest);
		if (sets1 > 10 || sets2 > 10 || games1 > 100 || games2 > 100 || points1 > 100 || points2 > 100)
			throw new InvalidArgumentException("Invalid current score");
		Player player1 = playerService.getPlayer(playerId1);
		Player player2 = playerService.getPlayer(playerId2);
		LocalDate aDate1 = dateForMatchup(dateSelector1, date1, date, player1);
		LocalDate aDate2 = dateForMatchup(dateSelector2, date2, date, player2);
		TournamentLevel tournamentLevel = TournamentLevel.safeDecode(level);
		MatchPrediction prediction = matchPredictionService.predictMatch(
			playerId1, playerId2, aDate1, aDate2,
			Surface.safeDecode(surface), indoor, tournamentLevel, Round.safeDecode(round)
      );
		PlayerPerformance perf1 = performanceService.getPlayerPerformance(playerId1, PerfStatsFilter.forOpponent(playerId2, level, surface, indoor, round));
		short bestOf = MatchDataUtil.defaultBestOf(tournamentLevel, null);
		MatchRules matchRules = bestOf == 3 ? MatchRules.BEST_OF_3_MATCH : MatchRules.BEST_OF_5_MATCH;
		SetRules setRules = matchRules.getSet(sets1 + sets2 + 1);
		boolean tieBreak = setRules.isTieBreak(games1, games2);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("rounds", Round.ROUNDS);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("date", date != null ? date : aDate1);
		modelMap.addAttribute("date1", aDate1);
		modelMap.addAttribute("date2", aDate2);
		modelMap.addAttribute("dateSelector1", dateSelector1);
		modelMap.addAttribute("dateSelector2", dateSelector2);
		modelMap.addAttribute("prediction", prediction);
		modelMap.addAttribute("priceFormat", priceFormat);
		modelMap.addAttribute("showDetails", showDetails);
		modelMap.addAttribute("perf1", perf1);
		modelMap.addAttribute("inMatch", inMatch);
		modelMap.addAttribute("sets", IntStream.rangeClosed(0, matchRules.getSets()).toArray());
		modelMap.addAttribute("games", IntStream.rangeClosed(0, setRules.hasTieBreak() ? setRules.getTieBreakAt() + 1 : 15).toArray());
		if (tieBreak)
			modelMap.addAttribute("tbPoints", IntStream.rangeClosed(0, setRules.getTieBreak().getPoints() + 5).toArray());
		else
			modelMap.addAttribute("points", GamePoint.values());
		modelMap.addAttribute("isTieBreak", tieBreak);
		modelMap.addAttribute("serve", serve);

		if (inMatch) {
			CurrentScore score = new CurrentScore(matchRules, sets1, sets2, games1, games2, points1, points2, serve);
			if (!isNullOrEmpty(command)) {
				switch (command) {
					case "S1": score.incSets1(); break;
					case "S2": score.incSets2(); break;
					case "G1": score.incGames1(); break;
					case "G2": score.incGames2(); break;
					case "P1": score.incPoints1(); break;
					case "P2": score.incPoints2(); break;
				}
				sets1 = score.getSets1();
				sets2 = score.getSets2();
				games1 = score.getGames1();
				games2 = score.getGames2();
				points1 = score.getPoints1();
				points2 = score.getPoints2();
				serve = score.getServe();
			}
			BaseProbabilities baseProbsH2H1 = toBaseProbabilities(statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2)));
			BaseProbabilities baseProbsLast52w1 = toBaseProbabilities(statisticsService.getPlayerStats(playerId1, MatchFilter.forSeason(MatchFilter.LAST_52_WEEKS_SEASON)));
			BaseProbabilities baseProbsLast52w2 = toBaseProbabilities(statisticsService.getPlayerStats(playerId2, MatchFilter.forSeason(MatchFilter.LAST_52_WEEKS_SEASON)));
			BaseProbabilities baseProbs1 = baseProbsH2H1.combine(baseProbsLast52w1.combine(baseProbsLast52w2.swap())).defaultIfUnknown();
			baseProbs1 = InMatchPredictor.normalize(prediction.getWinProbability1(), baseProbs1, matchRules);
			BaseProbabilities baseProbs2 = baseProbs1.swap();
			MatchOutcome matchOutcome = new MatchOutcome(baseProbs1.getPServe(), baseProbs1.getPReturn(), matchRules);
			MatchProbabilities inMatchProbs = matchOutcome.pWin(sets1, sets2, games1, games2, points1, points2, serve == 1);
			double pInMatch1 = inMatchProbs.getPMatch();
			double pInMatch2 = 1.0 - pInMatch1;
			double pInSet1 = inMatchProbs.getPSet();
			double pInSet2 = 1.0 - pInSet1;
			double pInGame1 = inMatchProbs.getPGame();
			double pInGame2 = 1.0 - pInGame1;
			double pSet1 = matchOutcome.getPSetWin();
			SetOutcome setOutcome = matchOutcome.getSetOutcome();
			double pSvcGame1 = setOutcome.getPServeWin();
			double pRtnGame1 = setOutcome.getPReturnWin();

			modelMap.addAttribute("sets1", sets1);
			modelMap.addAttribute("sets2", sets2);
			modelMap.addAttribute("games1", games1);
			modelMap.addAttribute("games2", games2);
			modelMap.addAttribute("points1", points1);
			modelMap.addAttribute("points2", points2);
			modelMap.addAttribute("serve", serve);
			modelMap.addAttribute("pInMatch1", pInMatch1);
			modelMap.addAttribute("pInMatch2", pInMatch2);
			modelMap.addAttribute("pInMatchSwing1", pInMatch1 - prediction.getWinProbability1());
			modelMap.addAttribute("pInMatchSwing2", pInMatch2 - prediction.getWinProbability2());
			modelMap.addAttribute("pInSet1", pInSet1);
			modelMap.addAttribute("pInSet2", pInSet2);
			modelMap.addAttribute("pInGame1", pInGame1);
			modelMap.addAttribute("pInGame2", pInGame2);
			modelMap.addAttribute("pSet1", pSet1);
			modelMap.addAttribute("pSet2", 1.0 - pSet1);
			modelMap.addAttribute("pSvcGame1", pSvcGame1);
			modelMap.addAttribute("pSvcGame2", 1.0 - pRtnGame1);
			modelMap.addAttribute("pRtnGame1", pRtnGame1);
			modelMap.addAttribute("pRtnGame2", 1.0 - pSvcGame1);
			modelMap.addAttribute("pServe1", baseProbs1.getPServe());
			modelMap.addAttribute("pServe2", baseProbs2.getPServe());
			modelMap.addAttribute("pReturn1", baseProbs1.getPReturn());
			modelMap.addAttribute("pReturn2", baseProbs2.getPReturn());
		}
		return new ModelAndView("h2hHypotheticalMatchup", modelMap);
	}

	private LocalDate dateForMatchup(String dateSelector, LocalDate playerDate, LocalDate date, Player player) {
		LocalDate aDate;
		if (!isNullOrEmpty(dateSelector))
			aDate = selectDate(player, dateSelector);
		else
			aDate = playerDate != null ? playerDate : date;
		return aDate != null ? aDate : defaultDate(player);
	}

	private LocalDate selectDate(Player player, String dateSelector) {
		int playerId = player.getId();
		switch (dateSelector) {
			case "Today": return LocalDate.now();
			case "CareerEnd": return playerService.getPlayerCareerEnd(playerId);
			case "PeakRank": return rankingsService.getRankingHighlights(playerId).getBestRankDate();
			case "PeakRankPoints": return rankingsService.getRankingHighlights(playerId).getBestRankPointsDate();
			case "PeakEloRank": return rankingsService.getRankingHighlights(playerId).getElo().getBestRankDate();
			case "PeakEloRating": return rankingsService.getRankingHighlights(playerId).getElo().getBestRatingDate();
			case "PeakHardEloRank": return rankingsService.getRankingHighlights(playerId).getHardElo().getBestRankDate();
			case "PeakHardEloRating": return rankingsService.getRankingHighlights(playerId).getHardElo().getBestRatingDate();
			case "PeakClayEloRank": return rankingsService.getRankingHighlights(playerId).getClayElo().getBestRankDate();
			case "PeakClayEloRating": return rankingsService.getRankingHighlights(playerId).getClayElo().getBestRatingDate();
			case "PeakGrassEloRank": return rankingsService.getRankingHighlights(playerId).getGrassElo().getBestRankDate();
			case "PeakGrassEloRating": return rankingsService.getRankingHighlights(playerId).getGrassElo().getBestRatingDate();
			case "PeakCarpetEloRank": return rankingsService.getRankingHighlights(playerId).getCarpetElo().getBestRankDate();
			case "PeakCarpetEloRating": return rankingsService.getRankingHighlights(playerId).getCarpetElo().getBestRatingDate();
			default: return null;
		}
	}

	private LocalDate defaultDate(Player player) {
		if (player.isActive())
			return LocalDate.now();
		else {
			LocalDate careerEndDate = playerService.getPlayerCareerEnd(player.getId());
			return careerEndDate != null ? careerEndDate : LocalDate.now();
		}
	}

	private static BaseProbabilities toBaseProbabilities(PlayerStats stats) {
		return stats.isEmpty() ? BaseProbabilities.UNKNOWN : new BaseProbabilities(stats.getServicePointsWonPct() / PCT, stats.getReturnPointsWonPct() / PCT);
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
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		return new ModelAndView("headsToHeads", modelMap);
	}

	@GetMapping("/headsToHeadsTable")
	public ModelAndView headsToHeadsTable(
		@RequestParam(name = "players") String playersCSV,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "statsVsAll") boolean statsVsAll,
		@RequestParam(name = "rawData", defaultValue = F) boolean rawData
	) {
		List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		RivalryFilter filter = new RivalryFilter(seasonRange, level, bestOf, surface, indoor, speedRange, round, null);

		List<Integer> playerIds = playerService.findPlayerIds(players);
		HeadsToHeads headsToHeads = rivalriesService.getHeadsToHeads(playerIds, filter);
		Map<Integer, PlayerStats> playersStats = statisticsService.getPlayersStats(playerIds, filter, statsVsAll);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("rawData", rawData);
		modelMap.addAttribute("headsToHeads", headsToHeads);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("playersStats", playersStats);
		modelMap.addAttribute("hasPointStats", playersStats.values().stream().anyMatch(PlayerStats::hasPointStats));
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
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournamentService.getTournaments());
		return new ModelAndView("greatestRivalries", modelMap);
	}

	@GetMapping("/greatestMatches")
	public ModelAndView greatestMatches() {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournamentService.getTournaments());
		modelMap.addAttribute("minMatchScore", MatchesService.MIN_MATCH_SCORE);
		return new ModelAndView("greatestMatches", modelMap);
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

	private Set<CountryCode> getOpponentCountriesUnion(int playerId1, int playerId2) {
		NavigableSet<CountryCode> countries = new TreeSet<>(comparing(CountryCode::getName));
		countries.addAll(matchesService.getOpponentCountries(playerId1));
		countries.addAll(matchesService.getOpponentCountries(playerId2));
		return countries;
	}

	private static <T> Set<T> union(Collection<T> col1, Collection<T> col2) {
		Set<T> union = new TreeSet<>(col1);
		union.addAll(col2);
		return union;
	}
}
