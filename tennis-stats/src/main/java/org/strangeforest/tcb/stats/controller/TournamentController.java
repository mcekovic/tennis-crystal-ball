package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TournamentController extends PageController {

	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;
	@Autowired private TournamentForecastService forecastService;

	private static final int MAX_RECORD_PLAYERS = 10;

	@GetMapping("/tournaments")
	public ModelAndView tournaments(
		@RequestParam(name = "level", required = false) String level
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		return new ModelAndView("tournaments", modelMap);
	}

	@GetMapping("/tournament")
	public ModelAndView tournament(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		Tournament tournament = tournamentService.getTournament(tournamentId);
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournament", tournament);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournament", modelMap);
	}

	@GetMapping("/tournamentEvents")
	public ModelAndView tournamentEvents(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
	) {
		List<Integer> seasons = dataService.getSeasons();
		List<TournamentItem> tournaments = tournamentService.getTournaments();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("tournaments", tournaments);
		return new ModelAndView("tournamentEvents", modelMap);
	}

	@GetMapping("/tournamentEvent")
	public ModelAndView tournamentEvent(
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		TournamentEvent tournamentEvent = tournamentService.getTournamentEvent(tournamentEventId);
		TournamentEventResults results = matchesService.getTournamentEventResults(tournamentEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEvent", tournamentEvent);
		modelMap.addAttribute("results", results);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournamentEvent", modelMap);
	}

	@GetMapping("/tournamentEventStats")
	public ModelAndView tournamentEventStats(
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", matchesService.getTournamentEventCountries(tournamentEventId));
		return new ModelAndView("tournamentEventStats", modelMap);
	}

	@GetMapping("/tournamentEventMap")
	public ModelAndView tournamentEventMap(
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		String mapProperties = tournamentService.getTournamentEventMapProperties(tournamentEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("mapProperties", mapProperties);
		return new ModelAndView("tournamentEventMap", modelMap);
	}

	@GetMapping("/tournamentRecords")
	public ModelAndView tournamentRecords(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		List<RecordDetailRow> recordTitles = tournamentService.getTournamentRecord(tournamentId, "W", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> recordFinals = tournamentService.getTournamentRecord(tournamentId, "F", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> recordSemiFinals = tournamentService.getTournamentRecord(tournamentId, "SF", MAX_RECORD_PLAYERS);
		List<RecordDetailRow> recordAppearances = tournamentService.getTournamentRecord(tournamentId, "RR", MAX_RECORD_PLAYERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("recordTitles", recordTitles);
		modelMap.addAttribute("recordFinals", recordFinals);
		modelMap.addAttribute("recordSemiFinals", recordSemiFinals);
		modelMap.addAttribute("recordAppearances", recordAppearances);
		return new ModelAndView("tournamentRecords", modelMap);
	}

	@GetMapping("/tournamentPerformance")
	public ModelAndView tournamentPerformance(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("categoryClasses", PerformanceCategory.getBasicCategoryClasses());
		modelMap.addAttribute("seasons", tournamentService.getTournamentSeasons(tournamentId));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", matchesService.getTournamentCountries(tournamentId));
		return new ModelAndView("tournamentPerformance", modelMap);
	}

	@GetMapping("/tournamentStats")
	public ModelAndView tournamentStats(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("seasons", tournamentService.getTournamentSeasons(tournamentId));
		modelMap.addAttribute("levels", TournamentLevel.ALL_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.ALL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("results", EventResult.values());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", matchesService.getTournamentCountries(tournamentId));
		return new ModelAndView("tournamentStats", modelMap);
	}

	@GetMapping("/inProgressEventsForecasts")
	public String inProgressEventsForecasts() {
		return "inProgressEventsForecasts";
	}

	@GetMapping("/inProgressEventForecast")
	public ModelAndView inProgressEventForecast(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
		@RequestParam(name = "tab", required = false) String tab,
      @RequestParam(name = "priceFormat", required = false) PriceFormat priceFormat
	) {
		InProgressEventForecast forecast = forecastService.getInProgressEventForecast(inProgressEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEventId", inProgressEventId);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("forecast", forecast);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		modelMap.addAttribute("priceFormats", PriceFormat.values());
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventForecast", modelMap);
	}

	@GetMapping("/inProgressEventResults")
	public ModelAndView inProgressEventResults(
		@RequestParam(name = "inProgressEventId") int inProgressEventId
	) {
		TournamentEventResults results = forecastService.getInProgressEventCompletedMatches(inProgressEventId);
		return new ModelAndView("inProgressEventResults", "results", results);
	}

	@GetMapping("/inProgressEventProbableMatches")
	public ModelAndView inProgressEventProbableMatches(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
		@RequestParam(name = "pinnedPlayerId", required = false) Integer pinnedPlayerId,
      @RequestParam(name = "priceFormat", required = false) PriceFormat priceFormat
	) {
		ProbableMatches probableMatches = forecastService.getInProgressEventProbableMatches(inProgressEventId, pinnedPlayerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEvent", probableMatches.getEvent());
		modelMap.addAttribute("results", probableMatches.getResults());
		modelMap.addAttribute("players", probableMatches.getPlayers());
		modelMap.addAttribute("pinnedPlayerId", pinnedPlayerId);
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventProbableMatches", modelMap);
	}

	@GetMapping("/inProgressEventFavorites")
	public ModelAndView inProgressEventFavorites(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
      @RequestParam(name = "priceFormat", required = false) PriceFormat priceFormat
	) {
		InProgressEventFavorites favorites = forecastService.getInProgressEventFavorites(inProgressEventId, 10);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surface", favorites.getSurface());
		modelMap.addAttribute("favorites", favorites.getFavorites());
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventFavorites", modelMap);
	}
}
