package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;
import com.neovisionaries.i18n.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class PlayerProfileController extends PageController {

	@Autowired private PlayerService playerService;
	@Autowired private RivalriesService rivalriesService;
	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;
	@Autowired private RankingsService rankingsService;
	@Autowired private PlayerTimelineService timelineService;
	@Autowired private PerformanceService performanceService;
	@Autowired private StatisticsService statisticsService;
	@Autowired private GOATPointsService goatPointsService;

	@GetMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "bigWin", defaultValue = "false") boolean bigWin,
		@RequestParam(name = "h2h", required = false) Integer h2h,
		@RequestParam(name = "matches", required = false) Integer matches,
		@RequestParam(name = "rankType", required = false) String rankType,
		@RequestParam(name = "bySeason", defaultValue = "false") boolean bySeason,
		@RequestParam(name = "infamous", required = false) Boolean infamous,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase
	) {
		if (playerId == null && name == null)
			throw new NotFoundException("Player", null);
		Player player = playerId != null ? playerService.getPlayer(playerId) : playerService.getPlayer(name);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("tab", tab);
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
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("outcome", outcome);
		modelMap.addAttribute("score", score);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("bigWin", bigWin);
		modelMap.addAttribute("h2h", h2h);
		modelMap.addAttribute("matches", matches);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("bySeason", bySeason);
		modelMap.addAttribute("infamous", infamous);
		modelMap.addAttribute("searchPhrase", searchPhrase);
		modelMap.addAttribute("params", ParamsUtil.INSTANCE);
		return new ModelAndView("playerProfile", modelMap);
	}

	@GetMapping("/playerProfileTab")
	public ModelAndView playerProfileTab(
		@RequestParam(name = "playerId") int playerId
	) {
		Player player = playerService.getPlayer(playerId);
		PlayerPerformance performance = performanceService.getPlayerPerformance(playerId);
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance);
		int seasonCount = playerService.getPlayerSeasons(playerId).size();
		Integer bestSeason = playerService.getPlayerBestSeason(playerId);
		BootgridTable<PlayerTournamentEvent> lastEvent = tournamentService.getPlayerTournamentEventsTable(playerId, TournamentEventResultFilter.EMPTY, "date DESC", 1, 1);
		Map<String, Integer> surfaceTitles = performanceService.getPlayerSurfaceTitles(playerId);
		WonDrawLost playerH2H = rivalriesService.getPlayerH2H(playerId).orElse(null);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("favoriteSurface", favoriteSurface);
		modelMap.addAttribute("seasonCount", seasonCount);
		modelMap.addAttribute("bestSeason", bestSeason);
		if (lastEvent.getTotal() > 0) {
			modelMap.addAttribute("lastEvent", lastEvent.getRows().get(0));
			modelMap.addAttribute("levels", TournamentLevel.asMap());
			modelMap.addAttribute("surfaces", Surface.asMap());
		}
		modelMap.addAttribute("performance", performance);
		modelMap.addAttribute("surfaceTitles", surfaceTitles);
		modelMap.addAttribute("playerH2H", playerH2H);
		return new ModelAndView("playerProfileTab", modelMap);
	}

	@GetMapping("/playerWikipediaUrl")
	public String playerWikipediaUrl(
		@RequestParam(name = "playerId") int playerId
	) {
		return "redirect:" + playerService.getPlayerWikipediaUrl(playerId);
	}

	@GetMapping("/playerSeason")
	public ModelAndView playerSeason(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		if (season == null)
			season = !seasons.isEmpty() ? seasons.get(0) : Integer.valueOf(LocalDate.now().getYear());
		Map<EventResult, List<PlayerTournamentEvent>> seasonHighlights = tournamentService.getPlayerSeasonHighlights(playerId, season, 4);
		PlayerPerformanceEx seasonPerf = performanceService.getPlayerPerformanceEx(playerId, PerfStatsFilter.forSeason(season));
		PlayerSeasonGOATPoints seasonGOATPoints = goatPointsService.getPlayerSeasonGOATPoints(playerId, season);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("seasonHighlights", seasonHighlights);
		modelMap.addAttribute("seasonPerf", seasonPerf);
		modelMap.addAttribute("seasonGOATPoints", seasonGOATPoints);
		return new ModelAndView("playerSeason", modelMap);
	}

	@GetMapping("/playerEvents")
	public ModelAndView playerEvents(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournamentItems(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("result", result);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		return new ModelAndView("playerEvents", modelMap);
	}

	@GetMapping("/playerMatches")
	public ModelAndView playerMatches(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "outcome", required = false) String outcome,
		@RequestParam(name = "score", required = false) String score,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "bigWin", defaultValue = "false") boolean bigWin,
		@RequestParam(name = "searchPhrase", required = false) String searchPhrase
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournamentItems(playerId);
		List<TournamentEventItem> tournamentEvents = tournamentService.getPlayerTournamentEventItems(playerId);
		List<CountryCode> countries = matchesService.getOpponentCountries(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("tournamentEvents", tournamentEvents);
		modelMap.addAttribute("countries", countries);
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
		if (!isNullOrEmpty(opponent)) {
			modelMap.addAttribute("opponent", opponent);
			if (opponent.startsWith(OpponentFilter.OPPONENT_PREFIX)) {
				int opponentId = Integer.parseInt(opponent.substring(OpponentFilter.OPPONENT_PREFIX.length()));
				modelMap.addAttribute("opponentName", playerService.getPlayerName(opponentId));
			}
		}
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("outcome", outcome);
		modelMap.addAttribute("score", score);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("bigWin", bigWin);
		modelMap.addAttribute("searchPhrase", searchPhrase);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
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
		modelMap.addAttribute("rankTypes", RankCategory.ELO.getRankTypes());
		return new ModelAndView("playerTimeline", modelMap);
	}

	@GetMapping("/playerRivalries")
	public ModelAndView playerRivalries(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "h2h", required = false) Integer h2h,
		@RequestParam(name = "matches", required = false) Integer matches
	) {
		String name = playerService.getPlayerName(playerId);
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournamentItems(playerId);
		List<CountryCode> countries = matchesService.getOpponentCountries(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("countries", countries);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("h2h", h2h);
		modelMap.addAttribute("matches", matches);
		return new ModelAndView("playerRivalries", modelMap);
	}

	@GetMapping("/playerRankings")
	public ModelAndView playerRankings(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "rankType", required = false) String rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "bySeason", defaultValue = "false") boolean bySeason
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		RankingHighlights rankingHighlights = rankingsService.getRankingHighlights(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", new int[] {playerId});
		modelMap.addAttribute("rankType", rankType);
		if (season != null) {
			modelMap.addAttribute("fromDate", "01-01-" + season);
			modelMap.addAttribute("toDate", "31-12-" + season);
		}
		modelMap.addAttribute("bySeason", bySeason);
		modelMap.addAttribute("highlights", rankingHighlights);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("rankCategories", RankCategory.values());
		return new ModelAndView("playerRankings", modelMap);
	}

	@GetMapping("/playerPerformance")
	public ModelAndView playerPerformance(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "opponent", required = false) String opponent,
		@RequestParam(name = "countryId", required = false) String countryId,
		@RequestParam(name = "advFilter", defaultValue = F) boolean advFilter,
		@RequestParam(name = "rawData", defaultValue = F) boolean rawData
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournamentItems(playerId);
		List<CountryCode> countries = matchesService.getOpponentCountries(playerId);
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PlayerPerformanceEx perf = performanceService.getPlayerPerformanceEx(playerId, new PerfStatsFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", countries);
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
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("advFilter", advFilter);
		modelMap.addAttribute("rawData", rawData);
		modelMap.addAttribute("perf", perf);
		return new ModelAndView("playerPerformance", modelMap);
	}

	@GetMapping("/playerPerformanceChart")
	public ModelAndView playerPerformanceChart(
		@RequestParam(name = "playerId") int playerId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", new int[] {playerId});
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getCategoryClasses());
		return new ModelAndView("playerPerformanceChart", modelMap);
	}

	@GetMapping("/playerStatsTab")
	public ModelAndView playerStatsTab(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "bestOf", required = false) Integer bestOf,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "round", required = false) String round,
		@RequestParam(name = "result", required = false) String result,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
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
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);
		List<TournamentItem> tournaments = tournamentService.getPlayerTournamentItems(playerId);
		List<CountryCode> countries = matchesService.getOpponentCountries(playerId);
		Range<LocalDate> dateRange = RangeUtil.toRange(fromDate, toDate);
		Range<Integer> speedRange = CourtSpeed.toSpeedRange(speed);
		OpponentFilter opponentFilter = OpponentFilter.forStats(opponent, matchesService.getSameCountryIds(countryId));
		PlayerStats stats = statisticsService.getPlayerStats(playerId, MatchFilter.forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, opponentFilter));

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("tournaments", tournaments);
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", countries);
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
		modelMap.addAttribute("opponent", opponent);
		modelMap.addAttribute("countryId", countryId);
		modelMap.addAttribute("advFilter", advFilter);
		modelMap.addAttribute("categoryGroups", StatsCategory.getCategoryGroups());
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("stats", stats);
		modelMap.addAttribute("rawData", rawData);
		modelMap.addAttribute("compare", compare);
		if (compare) {
			MatchFilter compareFilter = MatchFilter.forStats(compareSeason, compareLevel, compareSurface);
			PlayerStats compareStats = statisticsService.getPlayerStats(playerId, compareFilter);
			if (!compareStats.isEmpty())
				modelMap.addAttribute("compareStats", compareStats);
			modelMap.addAttribute("compareSeason", compareSeason);
			modelMap.addAttribute("compareLevel", compareLevel);
			modelMap.addAttribute("compareSurface", compareSurface);
			modelMap.addAttribute("relativeTo", relativeTo(compareSeason, compareLevel, compareSurface));
		}
		return new ModelAndView("playerStatsTab", modelMap);
	}

	@GetMapping("/playerStatsChart")
	public ModelAndView playerStatsChart(
		@RequestParam(name = "playerId") int playerId
	) {
		List<Integer> seasons = playerService.getPlayerSeasons(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", new int[] {playerId});
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("categoryTypes", StatsCategory.getCategoryTypes());
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		return new ModelAndView("playerStatsChart", modelMap);
	}

	@GetMapping("/playerTournaments")
	public ModelAndView playerTournaments(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "result", required = false) String result
	) {
		String name = playerService.getPlayerName(playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerName", name);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("result", result);
		return new ModelAndView("playerTournaments", modelMap);
	}

	@GetMapping("/playerGOATPoints")
	public ModelAndView playerGOATPoints(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "season", required = false) Integer season
	) {
		Map<String, Collection<String>> levelResults = goatPointsService.getLevelResults();
		PlayerGOATPoints goatPoints = goatPointsService.getPlayerGOATPoints(playerId, Surface.safeDecode(surface), true);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("levelResults", levelResults);
		modelMap.addAttribute("levelResultCount", levelResults.values().stream().mapToInt(Collection::size).sum());
		modelMap.addAttribute("goatPoints", goatPoints);
		modelMap.addAttribute("highlightSeason", season);
		return new ModelAndView("playerGOATPoints", modelMap);
	}

	@GetMapping("/playerRecords")
	public ModelAndView playerRecords(
		@RequestParam(name = "playerId") int playerId,
		@RequestParam(name = "infamous", required = false) Boolean infamous
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("infamous", infamous);
		modelMap.addAttribute("recordCategoryClasses", Records.getRecordCategoryClasses());
		modelMap.addAttribute("infamousRecordCategoryClasses", Records.getInfamousRecordCategoryClasses());
		return new ModelAndView("playerRecords", modelMap);
	}
}
