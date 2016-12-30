package org.strangeforest.tcb.stats.controller;

import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.base.*;

import static java.util.stream.Collectors.*;

@Controller
public class PlayerStatsController extends BaseController {

	@Autowired private PlayerService playerService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private PlayerTimelineService timelineService;

	private static final String PCT_FORMAT = "0.0'%'";
	private static final String RATIO_FORMAT = "0.00";
	private static final String PCT_DIFF_FORMAT = "+0.0'%';-0.0'%'";
	private static final String RATIO_DIFF_FORMAT = "+0.00;-0.00";

	@GetMapping("/eventsStats")
	public ModelAndView eventsStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "compare", defaultValue = "false") boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		MatchFilter filter = MatchFilter.forStats(season, level, surface, tournamentId, null, result, null, null, null, searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("stats", stats);
		addStatsFormats(modelMap);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("eventsStats", modelMap);
	}

	@GetMapping("/matchesStats")
	public ModelAndView matchesStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase,
		@RequestParam(name = "compare", defaultValue = "false") boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		MatchFilter filter = MatchFilter.forStats(season, level, surface, tournamentId, tournamentEventId, null, round, OpponentFilter.forStats(opponent), OutcomeFilter.forStats(outcome), searchPhrase);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("stats", stats);
		addStatsFormats(modelMap);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("matchesStats", modelMap);
	}

	@GetMapping("/eventStats")
	public ModelAndView eventStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "tournamentEventId") int tournamentEventId,
		@RequestParam(name = "compare", defaultValue = "false") boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		MatchFilter filter = MatchFilter.forTournamentEvent(tournamentEventId);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("stats", stats);
		addStatsFormats(modelMap);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("eventStats", modelMap);
	}

	@GetMapping("/rivalryStats")
	public ModelAndView rivalryStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "opponentId") int opponentId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "compare", defaultValue = "false") boolean compare,
		@RequestParam(name = "compareSeason", required = false) Integer compareSeason,
		@RequestParam(name = "compareLevel", required = false) String compareLevel,
		@RequestParam(name = "compareSurface", required = false) String compareSurface
	) {
		MatchFilter filter = MatchFilter.forOpponent(opponentId, level, surface, round);
		PlayerStats stats = statisticsService.getPlayerStats(playerId, filter);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("opponentId", opponentId);
		modelMap.addAttribute("stats", stats);
		addStatsFormats(modelMap);
		addCompareStats(modelMap, playerId, compare, compareSeason, compareLevel, compareSurface);
		return new ModelAndView("rivalryStats", modelMap);
	}

	@GetMapping("/matchStats")
	public ModelAndView matchStats(
		@RequestParam(name = "matchId") long matchId
	) {
		MatchStats matchStats = statisticsService.getMatchStats(matchId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("matchId", matchId);
		modelMap.addAttribute("matchStats", matchStats);
		addStatsFormats(modelMap);
		return new ModelAndView("matchStats", modelMap);
	}

	@GetMapping("/playerTimelineStats")
	public ModelAndView playerTimelineStats(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		PlayerStats careerStats = statisticsService.getPlayerStats(playerId);
		List<Integer> seasonList = toSeasons(seasons);
		Map<Integer, PlayerStats> seasonsStats = statisticsService.getPlayerSeasonsStats(playerId);
		ensureSeasons(seasonsStats, seasonList, PlayerStats.EMPTY);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("careerStats", careerStats);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsStats", seasonsStats);
		return new ModelAndView("playerTimelineStats", modelMap);
	}

	@GetMapping("/playerTimelinePerformance")
	public ModelAndView playerTimelinePerformance(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "seasons") String seasons
	) {
		Map<Integer, Integer> titles = timelineService.getPlayerSeasonTitles(playerId);
		Map<Integer, Integer> yearEndRanks = timelineService.getPlayerYearEndRanks(playerId);
		Map<Integer, Integer> yearEndEloRatings = timelineService.getPlayerYearEndEloRatings(playerId);
		Map<Integer, Integer> goatPoints = timelineService.getPlayerSeasonGOATPoints(playerId);
		PlayerPerformance careerPerf = statisticsService.getPlayerPerformance(playerId);
		List<Integer> seasonList = toSeasons(seasons);
		Map<Integer, PlayerPerformance> seasonsPerf = statisticsService.getPlayerSeasonsPerformance(playerId);
		ensureSeasons(seasonsPerf, seasonList, PlayerPerformance.EMPTY);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("titles", titles);
		modelMap.addAttribute("yearEndRanks", yearEndRanks);
		modelMap.addAttribute("yearEndEloRatings", yearEndEloRatings);
		modelMap.addAttribute("goatPoints", goatPoints);
		modelMap.addAttribute("careerPerf", careerPerf);
		modelMap.addAttribute("seasons", seasonList);
		modelMap.addAttribute("seasonsPerf", seasonsPerf);
		return new ModelAndView("playerTimelinePerformance", modelMap);
	}

	private List<Integer> toSeasons(String seasons) {
		return Stream.of(seasons.split(",")).map(Integer::valueOf).collect(toList());
	}

	private <T> void ensureSeasons(Map<Integer, T> seasonsData, List<Integer> seasons, T empty) {
		for (Integer season : seasons) {
			if (!seasonsData.containsKey(season))
				seasonsData.put(season, empty);
		}
	}


	// Util

	private static void addStatsFormats(ModelMap modelMap) {
		modelMap.addAttribute("pctFormat", new DecimalFormat(PCT_FORMAT));
		modelMap.addAttribute("pctDiffFormat", new DecimalFormat(PCT_DIFF_FORMAT));
		modelMap.addAttribute("ratioFormat", new DecimalFormat(RATIO_FORMAT));
		modelMap.addAttribute("ratioDiffFormat", new DecimalFormat(RATIO_DIFF_FORMAT));
		modelMap.addAttribute("statsFormatUtil", StatsFormatUtil.INSTANCE);
	}

	private void addCompareStats(ModelMap modelMap, int playerId, boolean compare, Integer compareSeason, String compareLevel, String compareSurface) {
		modelMap.addAttribute("compare", compare);
		if (compare) {
			MatchFilter compareFilter = MatchFilter.forStats(compareSeason, compareLevel, compareSurface);
			PlayerStats compareStats = statisticsService.getPlayerStats(playerId, compareFilter);
			if (!compareStats.isEmpty())
				modelMap.addAttribute("compareStats", compareStats);
		}
		modelMap.addAttribute("seasons", playerService.getPlayerSeasons(playerId));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("compareSeason", compareSeason);
		modelMap.addAttribute("compareLevel", compareLevel);
		modelMap.addAttribute("compareSurface", compareSurface);
		modelMap.addAttribute("relativeTo", relativeTo(compareSeason, compareLevel, compareSurface));
	}

	private static String relativeTo(Integer compareSeason, String compareLevel, String compareSurface) {
		StringBuilder relativeTo = new StringBuilder();
		if (compareSeason != null)
			relativeTo.append(compareSeason);
		if (!Strings.isNullOrEmpty(compareLevel)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append(TournamentLevel.decode(compareLevel).getText());
		}
		if (!Strings.isNullOrEmpty(compareSurface)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append(Surface.decode(compareSurface).getText());
		}
		return relativeTo.length() > 0 ? relativeTo.toString() : "Career";
	}
}
