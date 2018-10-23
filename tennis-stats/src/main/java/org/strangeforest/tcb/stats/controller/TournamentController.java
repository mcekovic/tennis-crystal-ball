package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.forecast.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

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
		modelMap.addAttribute("speeds", CourtSpeed.values());
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
		@RequestParam(name = "speed", required = false) Integer speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
	) {
		List<Integer> seasons = dataService.getSeasons();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		modelMap.addAttribute("tournaments", tournamentService.getTournaments());
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
		modelMap.addAttribute("speeds", CourtSpeed.values());
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
		modelMap.addAttribute("speeds", CourtSpeed.values());
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
		@RequestParam(name = "inProgressEventId", required = false) Integer inProgressEventId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "tab", required = false) String tab,
		@RequestParam(name = "eloType", defaultValue = "OVERALL") ForecastEloType eloType,
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat
	) {
		if (inProgressEventId == null) {
			if (name != null)
				inProgressEventId = forecastService.findInProgressEventId(name);
			else
				throw new NotFoundException("In-Progress Event", null);
		}
		InProgressEventForecast forecast = forecastService.getInProgressEventForecast(inProgressEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEventId", inProgressEventId);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("forecast", forecast);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		modelMap.addAttribute("eloTypes", ForecastEloType.values());
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("priceFormat", priceFormat);
		modelMap.addAttribute("params", ParamsUtil.INSTANCE);
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
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@RequestParam(name = "eloType", defaultValue = "OVERALL") ForecastEloType eloType,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat
	) {
		ProbableMatches probableMatches = forecastService.getInProgressEventProbableMatches(inProgressEventId, playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEvent", probableMatches.getEvent());
		modelMap.addAttribute("results", probableMatches.getResults());
		modelMap.addAttribute("players", probableMatches.getPlayers());
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventProbableMatches", modelMap);
	}

	@GetMapping("/inProgressEventPlayerPath")
	public ModelAndView inProgressEventPlayerPath(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@RequestParam(name = "eloType", defaultValue = "OVERALL") ForecastEloType eloType,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat
	) {
		PlayerPath playerPath = forecastService.getInProgressEventPlayerPath(inProgressEventId, playerId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEvent", playerPath.getInProgressEvent());
		modelMap.addAttribute("players", playerPath.getPlayers());
		modelMap.addAttribute("playerId", playerId);
		modelMap.addAttribute("playerPath", playerPath);
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventPlayerPath", modelMap);
	}

	@GetMapping("/inProgressEventFavorites")
	public ModelAndView inProgressEventFavorites(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
		@RequestParam(name = "eloType", defaultValue = "OVERALL") ForecastEloType eloType,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat
	) {
		InProgressEventFavorites favorites = forecastService.getInProgressEventFavorites(inProgressEventId, 10, eloType);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("surface", favorites.getSurface());
		modelMap.addAttribute("indoor", favorites.isIndoor());
		modelMap.addAttribute("favorites", favorites.getFavorites());
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventFavorites", modelMap);
	}
}
