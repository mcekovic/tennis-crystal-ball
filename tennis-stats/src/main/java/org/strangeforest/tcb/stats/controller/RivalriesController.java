package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.time.temporal.*;
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
import static java.lang.Math.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.controller.RankingsController.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.stats.service.MatchPredictionService.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;
import static org.strangeforest.tcb.util.UserAgentUtil.*;

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
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "rankType", required = false) String rankType
	) {
		var player1 = playerId1 != null ? playerService.getPlayer(playerId1) : (name1 != null ? playerService.getPlayer(name1) : null);
		var player2 = playerId2 != null ? playerService.getPlayer(playerId2) : (name2 != null ? playerService.getPlayer(name2) : null);

		var modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("season1", season1);
		modelMap.addAttribute("season2", season2);
		modelMap.addAttribute("date", date);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("bestOf", bestOf);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("outcome", outcome);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("params", ParamsUtil.INSTANCE);
		return new ModelAndView("headToHead", modelMap);
	}

	@GetMapping("/h2hProfiles")
	public ModelAndView h2hProfiles(
		@RequestParam(name = "playerId1") int playerId1,
		@RequestParam(name = "playerId2") int playerId2
	) {
		var player1 = playerService.getPlayer(playerId1);
		var player2 = playerService.getPlayer(playerId2);
		var performance1 = performanceService.getPlayerPerformance(playerId1);
		var performance2 = performanceService.getPlayerPerformance(playerId2);
		var favoriteSurface1 = new FavoriteSurface(performance1);
		var favoriteSurface2 = new FavoriteSurface(performance2);
		var seasonCount1 = playerService.getPlayerSeasons(playerId1).size();
		var seasonCount2 = playerService.getPlayerSeasons(playerId2).size();
		var bestSeason1 = playerService.getPlayerBestSeason(playerId1);
		var bestSeason2 = playerService.getPlayerBestSeason(playerId2);
		var lastEvent1 = tournamentService.getPlayerTournamentEventsTable(playerId1, TournamentEventResultFilter.EMPTY, "date DESC", 1, 1);
		var lastEvent2 = tournamentService.getPlayerTournamentEventsTable(playerId2, TournamentEventResultFilter.EMPTY, "date DESC", 1, 1);
		var surfaceTitles1 = performanceService.getPlayerSurfaceTitles(playerId1);
		var surfaceTitles2 = performanceService.getPlayerSurfaceTitles(playerId2);
		var teamTitles1 = performanceService.getPlayerTeamTitles(playerId1);
		var teamTitles2 = performanceService.getPlayerTeamTitles(playerId2);
		var playerH2H1 = rivalriesService.getPlayerH2H(playerId1).orElse(null);
		var playerH2H2 = rivalriesService.getPlayerH2H(playerId2).orElse(null);

		var matches1 = matchesService.getPlayerMatchesTable(playerId1, MatchFilter.forOpponent(playerId2, OutcomeFilter.PLAYED), false, "match_id", 1000, 1).getRows();
		var perf1 = performanceService.getPlayerPerformance(playerId1, PerfStatsFilter.forOpponent(playerId2));
		var h2h = new H2H(playerId1, playerId2, matches1);
		var surfaceAdjH2H = AdjustedH2H.surfaceAdjustedH2H(perf1, performance1, performance2);
		var importanceAdjH2H = AdjustedH2H.importanceAdjustedH2H(playerId1, playerId2, perf1, matches1, goatLegendService.getBigWinMatchFactors());
		var adjustedH2H = surfaceAdjH2H.add(importanceAdjH2H).scale(0.5);

		var modelMap = new ModelMap();
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
		modelMap.addAttribute("teamTitles1", teamTitles1);
		modelMap.addAttribute("teamTitles2", teamTitles2);
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
		var seasons = getSeasonsUnion(playerId1, playerId2);
		var defaultSeason = !seasons.isEmpty() ? seasons.first() : LocalDate.now().getYear();
		var defaultSeasons = season != null && season >= 0 ? season : defaultSeason;
		if (season == null)
			season = season1 == null && season2 == null ? defaultSeason : -1;
		if (season1 == null)
			season1 = defaultSeasons;
		if (season2 == null)
			season2 = defaultSeasons;
		var seasonHighlights1 = tournamentService.getPlayerSeasonHighlights(playerId1, season1, 4);
		var seasonHighlights2 = tournamentService.getPlayerSeasonHighlights(playerId2, season2, 4);
		var eventResults = union(seasonHighlights1.keySet(), seasonHighlights2.keySet()).stream().limit(4).collect(toList());
		var seasonPerf1 = performanceService.getPlayerPerformanceEx(playerId1, PerfStatsFilter.forSeason(season1));
		var seasonPerf2 = performanceService.getPlayerPerformanceEx(playerId2, PerfStatsFilter.forSeason(season2));
		var surfaces = union(seasonPerf1.getSurfaceMatches().keySet(), seasonPerf2.getSurfaceMatches().keySet());
		var levels = union(seasonPerf1.getLevelMatches().keySet(), seasonPerf2.getLevelMatches().keySet());
		var oppositions = union(seasonPerf1.getOppositionMatches().keySet(), seasonPerf2.getOppositionMatches().keySet());
		var rounds = union(seasonPerf1.getRoundMatches().keySet(), seasonPerf2.getRoundMatches().keySet());
		var seasonGOATPoints1 = goatPointsService.getPlayerSeasonGOATPoints(playerId1, season1);
		var seasonGOATPoints2 = goatPointsService.getPlayerSeasonGOATPoints(playerId2, season2);

		var modelMap = new ModelMap();
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
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "outcome", required = false) String outcome
   ) {
		var player1 = playerService.getPlayer(playerId1);
		var player2 = playerService.getPlayer(playerId2);
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2, season, dateRange, level, bestOf, surface, indoor, speedRange, round, tournamentId, outcome, null));

		var modelMap = new ModelMap();
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
		modelMap.addAttribute("outcome", outcome);
		return new ModelAndView("h2hMatches", modelMap);
	}

	@GetMapping("/h2hRankings")
	public ModelAndView h2hRankings(
		@RequestParam(name = "playerId") int[] playerId,
		@RequestParam(name = "rankType", required = false) String rankType
	) {
		var seasons = playerService.getPlayersSeasons(playerId);

		var modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("rankCategories", RankCategory.values());
		modelMap.addAttribute("referenceRanks", REFERENCE_RANKS);
		modelMap.addAttribute("seasons", seasons);
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
		var countryIds = matchesService.getSameCountryIds(countryId);
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter1 = h2h ? OpponentFilter.forStats(playerId2) : OpponentFilter.forStats(opponent, countryIds);
		var opponentFilter2 = h2h ? OpponentFilter.forStats(playerId1) : OpponentFilter.forStats(opponent, countryIds);
		var perf1 = performanceService.getPlayerPerformanceEx(playerId1, new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter1));
		var perf2 = performanceService.getPlayerPerformanceEx(playerId2, new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter2));
		var perfSurfaces = union(perf1.getSurfaceMatches().keySet(), perf2.getSurfaceMatches().keySet());
		var perfIndoors = union(perf1.getOutdoorIndoorMatches().keySet(), perf2.getOutdoorIndoorMatches().keySet());
		var perfSpeeds = union(perf1.getSpeedMatches().keySet(), perf2.getSpeedMatches().keySet());
		var perfLevels = union(perf1.getLevelMatches().keySet(), perf2.getLevelMatches().keySet());
		var perfBestOfs = union(perf1.getBestOfMatches().keySet(), perf2.getBestOfMatches().keySet());
		var perfOppositions = union(perf1.getOppositionMatches().keySet(), perf2.getOppositionMatches().keySet());
		var perfScores = union(perf1.getScoreCounts().keySet(), perf2.getScoreCounts().keySet());
		var perfRounds = union(perf1.getRoundMatches().keySet(), perf2.getRoundMatches().keySet());
		var perfResults = union(perf1.getResultCounts().keySet(), perf2.getResultCounts().keySet());

		var modelMap = new ModelMap();
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
		var seasons = playerService.getPlayersSeasons(playerId);

		var modelMap = new ModelMap();
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
		var countryIds = matchesService.getSameCountryIds(countryId);
		var dateRange = RangeUtil.toRange(fromDate, toDate);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var opponentFilter1 = h2h ? OpponentFilter.forStats(playerId2) : OpponentFilter.forStats(opponent, countryIds);
		var opponentFilter2 = h2h ? OpponentFilter.forStats(playerId1) : OpponentFilter.forStats(opponent, countryIds);
		var stats1 = statisticsService.getPlayerStats(playerId1, MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter1));
		var stats2 = statisticsService.getPlayerStats(playerId2, MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter2));

		var modelMap = new ModelMap();
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
			var compareFilter1 = MatchFilter.forStats(compareSeason, compareLevel, compareSurface, h2h ? OpponentFilter.forStats(playerId2) : null);
			var compareFilter2 = MatchFilter.forStats(compareSeason, compareLevel, compareSurface, h2h ? OpponentFilter.forStats(playerId1) : null);
			var compareStats1 = statisticsService.getPlayerStats(playerId1, compareFilter1);
			var compareStats2 = statisticsService.getPlayerStats(playerId2, compareFilter2);
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
		var seasons = playerService.getPlayersSeasons(playerId);

		var modelMap = new ModelMap();
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
		var aSurface = Surface.safeDecode(surface);
		var goatPoints1 = goatPointsService.getPlayerGOATPoints(playerId1, aSurface, false);
		var goatPoints2 = goatPointsService.getPlayerGOATPoints(playerId2, aSurface, false);
		Set<Integer> seasons = new TreeSet<>(reverseOrder());
		seasons.addAll(goatPoints1.getPlayerSeasons());
		seasons.addAll(goatPoints2.getPlayerSeasons());

		var modelMap = new ModelMap();
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
      @RequestParam(name = "tournamentId", required = false) Integer tournamentId,
      @RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
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
		rejectAgents(httpRequest, ROBOTS_AND_UNKNOWN);
		if (sets1 > 10 || sets2 > 10 || games1 > 100 || games2 > 100 || points1 > 100 || points2 > 100)
			throw new InvalidArgumentException("Invalid current score");
		var player1 = playerService.getPlayer(playerId1);
		var player2 = playerService.getPlayer(playerId2);
		var aDate1 = dateForMatchup(dateSelector1, date1, date, player1);
		var aDate2 = dateForMatchup(dateSelector2, date2, date, player2);
		var today = LocalDate.now();
		var inProgress = abs(ChronoUnit.DAYS.between(aDate1, today)) <= IN_PROGRESS_DAYS_THRESHOLD && abs(ChronoUnit.DAYS.between(aDate2, today)) <= IN_PROGRESS_DAYS_THRESHOLD;
		var tournamentLevel = TournamentLevel.safeDecode(level);
		var prediction = matchPredictionService.predictMatch(
			playerId1, playerId2, aDate1, aDate2, tournamentId, tournamentEventId, inProgress,
			Surface.safeDecode(surface), indoor, tournamentLevel, Round.safeDecode(round)
      );
		var perf1 = performanceService.getPlayerPerformance(playerId1, PerfStatsFilter.forOpponent(playerId2, level, surface, indoor, round));
		var bestOf = MatchDataUtil.defaultBestOf(tournamentLevel, null);
		var matchRules = bestOf == 3 ? MatchRules.BEST_OF_3_MATCH : MatchRules.BEST_OF_5_MATCH;
		var setRules = matchRules.getSet(sets1 + sets2 + 1);
		var tieBreak = setRules.isTieBreak(games1, games2);

		var modelMap = new ModelMap();
		modelMap.addAttribute("player1", player1);
		modelMap.addAttribute("player2", player2);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("rounds", Round.ROUNDS);
		modelMap.addAttribute("tournaments", tournamentService.getTournaments());
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("round", round);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
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
			var score = new CurrentScore(matchRules, sets1, sets2, games1, games2, points1, points2, serve);
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
			var baseProbsH2H1 = toBaseProbabilities(statisticsService.getPlayerStats(playerId1, MatchFilter.forOpponent(playerId2)));
			var baseProbsLast52w1 = toBaseProbabilities(statisticsService.getPlayerStats(playerId1, MatchFilter.forSeason(MatchFilter.LAST_52_WEEKS_SEASON)));
			var baseProbsLast52w2 = toBaseProbabilities(statisticsService.getPlayerStats(playerId2, MatchFilter.forSeason(MatchFilter.LAST_52_WEEKS_SEASON)));
			var baseProbs1 = baseProbsH2H1.combine(baseProbsLast52w1.combine(baseProbsLast52w2.swap())).defaultIfUnknown();
			baseProbs1 = InMatchPredictor.normalize(prediction.getWinProbability1(), baseProbs1, matchRules);
			var baseProbs2 = baseProbs1.swap();
			var matchOutcome = new MatchOutcome(baseProbs1.getPServe(), baseProbs1.getPReturn(), matchRules);
			var inMatchProbs = matchOutcome.pWin(sets1, sets2, games1, games2, points1, points2, serve == 1);
			var pInMatch1 = inMatchProbs.getPMatch();
			var pInMatch2 = 1.0 - pInMatch1;
			var pInSet1 = inMatchProbs.getPSet();
			var pInSet2 = 1.0 - pInSet1;
			var pInGame1 = inMatchProbs.getPGame();
			var pInGame2 = 1.0 - pInGame1;
			var pSet1 = matchOutcome.getPSetWin();
			var setOutcome = matchOutcome.getSetOutcome();
			var pSvcGame1 = setOutcome.getPServeWin();
			var pRtnGame1 = setOutcome.getPReturnWin();

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
		var playerId = player.getId();
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
			var careerEndDate = playerService.getPlayerCareerEnd(player.getId());
			return careerEndDate != null ? careerEndDate : LocalDate.now();
		}
	}

	private static BaseProbabilities toBaseProbabilities(PlayerStats stats) {
		return stats.hasPointStats() ? new BaseProbabilities(stats.getServicePointsWonPct() / PCT, stats.getReturnPointsWonPct() / PCT) : BaseProbabilities.UNKNOWN;
	}


	@GetMapping("/headsToHeads")
	public ModelAndView headsToHeads() {
		var modelMap = new ModelMap();
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
		var players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());
		var seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		var speedRange = CourtSpeed.toSpeedRange(speed);
		var filter = new RivalryFilter(seasonRange, level, bestOf, surface, indoor, speedRange, round, null);

		var playerIds = playerService.findPlayerIds(players);
		var headsToHeads = rivalriesService.getHeadsToHeads(playerIds, filter);
		var playersStats = statisticsService.getPlayersStats(playerIds, filter, statsVsAll);

		var modelMap = new ModelMap();
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
		var modelMap = new ModelMap();
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
		var modelMap = new ModelMap();
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
		return intersection(playerService.getPlayerSeasons(playerId1), playerService.getPlayerSeasons(playerId2));
	}

	private SortedSet<Integer> getSeasonsUnion(int playerId1, int playerId2) {
		return union(playerService.getPlayerSeasons(playerId1), playerService.getPlayerSeasons(playerId2), reverseOrder());
	}

	private Set<CountryCode> getOpponentCountriesUnion(int playerId1, int playerId2) {
		return union(matchesService.getOpponentCountries(playerId1), matchesService.getOpponentCountries(playerId2), comparing(CountryCode::getName));
	}

	private static <T> List<T> intersection(List<T> col1, List<T> col2) {
		return col1.stream().filter(col2::contains).collect(toList());
	}

	private static <T> Set<T> union(Collection<T> col1, Collection<T> col2) {
		Set<T> union = new TreeSet<>(col1);
		union.addAll(col2);
		return union;
	}

	private static <T> SortedSet<T> union(Collection<T> col1, Collection<T> col2, Comparator<T> comparator) {
		SortedSet<T> union = new TreeSet<>(comparator);
		union.addAll(col1);
		union.addAll(col2);
		return union;
	}
}
