package org.strangeforest.tcb.stats.controller;

import java.util.*;
import javax.servlet.http.*;

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

import static org.strangeforest.tcb.util.UserAgentUtil.*;

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
		var modelMap = new ModelMap();
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelGroups", TournamentLevelGroup.INDIVIDUAL_LEVEL_GROUPS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surfaceGroups", SurfaceGroup.values());
		modelMap.addAttribute("speeds", CourtSpeed.values());
		return new ModelAndView("tournaments", modelMap);
	}

	@GetMapping("/tournament")
	public ModelAndView tournament(
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "extId", required = false) String extId,
		@RequestParam(name = "tab", required = false) String tab
	) {
		if (tournamentId == null) {
			if (name != null)
				tournamentId = tournamentService.findTournamentId(name, extId);
			else
				throw new NotFoundException("Tournament", null);
		}

		var tournament = tournamentService.getTournament(tournamentId);
		var modelMap = new ModelMap();
		modelMap.addAttribute("tournament", tournament);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournament", modelMap);
	}

	@GetMapping("/tournamentEventsTab")
	public ModelAndView tournamentEventsTab(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("tournamentId", tournamentId);
		return new ModelAndView("tournamentEventsTab", modelMap);
	}

	@GetMapping("/tournamentPerformance")
	public ModelAndView tournamentPerformance(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		var modelMap = new ModelMap();
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
		var modelMap = new ModelMap();
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

	@GetMapping("/tournamentRecords")
	public ModelAndView tournamentRecords(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		var recordTitles = tournamentService.getTournamentRecord(tournamentId, "W", MAX_RECORD_PLAYERS);
		var recordFinals = tournamentService.getTournamentRecord(tournamentId, "F", MAX_RECORD_PLAYERS);
		var recordSemiFinals = tournamentService.getTournamentRecord(tournamentId, "SF", MAX_RECORD_PLAYERS);
		var recordAppearances = tournamentService.getTournamentRecord(tournamentId, "RR", MAX_RECORD_PLAYERS);

		var modelMap = new ModelMap();
		modelMap.addAttribute("recordTitles", recordTitles);
		modelMap.addAttribute("recordFinals", recordFinals);
		modelMap.addAttribute("recordSemiFinals", recordSemiFinals);
		modelMap.addAttribute("recordAppearances", recordAppearances);
		return new ModelAndView("tournamentRecords", modelMap);
	}

	@GetMapping("/tournamentGOATPoints")
	public ModelAndView tournamentGOATPoints(
		@RequestParam(name = "tournamentId") int tournamentId
	) {
		var recordGOATPoints = tournamentService.getTournamentGOATPoints(tournamentId, MAX_RECORD_PLAYERS);

		var modelMap = new ModelMap();
		modelMap.addAttribute("recordGOATPoints", recordGOATPoints);
		return new ModelAndView("tournamentGOATPoints", modelMap);
	}

	@GetMapping("/tournamentEvents")
	public ModelAndView tournamentEvents(
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "level", required = false) String level,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "indoor", required = false) Boolean indoor,
		@RequestParam(name = "speed", required = false) String speed,
		@RequestParam(name = "tournamentId", required = false) Integer tournamentId
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("level", level);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("speed", speed);
		modelMap.addAttribute("tournamentId", tournamentId);
		modelMap.addAttribute("seasons", dataService.getSeasons());
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
		@RequestParam(name = "tournamentEventId", required = false) Integer tournamentEventId,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "extId", required = false) String extId,
		@RequestParam(name = "season", required = false) Integer season
	) {
		if (tournamentEventId == null) {
			if (name != null && season != null)
				tournamentEventId = tournamentService.findTournamentEventId(name, extId, season);
			else
				throw new NotFoundException("Tournament Event", name);
		}

		var tournamentEvent = tournamentService.getTournamentEvent(tournamentEventId);
		var results = matchesService.getTournamentEventResults(tournamentEventId);

		var modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEvent", tournamentEvent);
		modelMap.addAttribute("results", results);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournamentEvent", modelMap);
	}

	@GetMapping("/tournamentEventSeeds")
	public ModelAndView tournamentEventSeeds(
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		var seeds = tournamentService.getTournamentEventSeeds(tournamentEventId);

		var modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEventId", tournamentEventId);
		modelMap.addAttribute("seeds", seeds);
		return new ModelAndView("tournamentEventSeeds", modelMap);
	}

	@GetMapping("/tournamentEventStats")
	public ModelAndView tournamentEventStats(
		@RequestParam(name = "tournamentEventId") int tournamentEventId
	) {
		var modelMap = new ModelMap();
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
		var mapProperties = tournamentService.getTournamentEventMapProperties(tournamentEventId);

		var modelMap = new ModelMap();
		modelMap.addAttribute("mapProperties", mapProperties);
		return new ModelAndView("tournamentEventMap", modelMap);
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
		var forecast = forecastService.getInProgressEventForecast(inProgressEventId);
		var events = forecastService.getInProgressEventsTable(InProgressEventFilter.ALL_IN_PROGRESS, priceFormat, InProgressEventsResource.defaultOrderBy(), 20, 1).getRows();
		var eventCount = events.size();
		var currentEventIndex = findEventIndex(events, inProgressEventId);

		var modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEventId", inProgressEventId);
		modelMap.addAttribute("tab", tab);
		modelMap.addAttribute("forecast", forecast);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		modelMap.addAttribute("eloTypes", ForecastEloType.values());
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("playerId", playerId);
		if (currentEventIndex >= 0 && eventCount > 1) {
			modelMap.addAttribute("prevEvent", events.get((currentEventIndex + 1) % eventCount));
			modelMap.addAttribute("nextEvent", events.get((currentEventIndex - 1 + eventCount) % eventCount));
		}
		modelMap.addAttribute("priceFormat", priceFormat);
		modelMap.addAttribute("params", ParamsUtil.INSTANCE);
		return new ModelAndView("inProgressEventForecast", modelMap);
	}

	private int findEventIndex(List<InProgressEvent> events, int inProgressEventId) {
		for (int i = 0, count = events.size(); i < count; i++) {
			var event = events.get(i);
			if (event.getId() == inProgressEventId)
				return i;
		}
		return -1;
	}

	@GetMapping("/inProgressEventResults")
	public ModelAndView inProgressEventResults(
		@RequestParam(name = "inProgressEventId") int inProgressEventId
	) {
		var results = forecastService.getInProgressEventCompletedMatches(inProgressEventId);
		return new ModelAndView("inProgressEventResults", "results", results);
	}

	@GetMapping("/inProgressEventProbableMatches")
	public ModelAndView inProgressEventProbableMatches(
		@RequestParam(name = "inProgressEventId") int inProgressEventId,
		@RequestParam(name = "playerId", required = false) Integer playerId,
		@RequestParam(name = "eloType", defaultValue = "OVERALL") ForecastEloType eloType,
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat,
      HttpServletRequest httpRequest
	) {
		rejectAgents(httpRequest, ROBOTS_AND_UNKNOWN);
		var probableMatches = forecastService.getInProgressEventProbableMatches(inProgressEventId, playerId);

		var modelMap = new ModelMap();
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
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat,
      HttpServletRequest httpRequest
	) {
		rejectAgents(httpRequest, ROBOTS_AND_UNKNOWN);
		var playerPath = forecastService.getInProgressEventPlayerPath(inProgressEventId, playerId);

		var modelMap = new ModelMap();
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
		@CookieValue(value = "priceFormat", required = false) PriceFormat priceFormat,
      HttpServletRequest httpRequest
	) {
		rejectAgents(httpRequest, ROBOTS_AND_UNKNOWN);
		var favorites = forecastService.getInProgressEventFavorites(inProgressEventId, eloType);

		var modelMap = new ModelMap();
		modelMap.addAttribute("surface", favorites.getSurface());
		modelMap.addAttribute("indoor", favorites.isIndoor());
		modelMap.addAttribute("favorites", favorites.getFavorites());
		modelMap.addAttribute("eloType", eloType);
		modelMap.addAttribute("priceFormat", priceFormat);
		return new ModelAndView("inProgressEventFavorites", modelMap);
	}

	@GetMapping("/inProgressEventStats")
	public ModelAndView inProgressEventStats(
		@RequestParam(name = "inProgressEventId") int inProgressEventId
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("inProgressEventId", inProgressEventId);
		modelMap.addAttribute("categoryClasses", StatsCategory.getCategoryClasses());
		modelMap.addAttribute("rounds", Round.values());
		modelMap.addAttribute("opponentCategories", Opponent.categories());
		modelMap.addAttribute("countries", matchesService.getInProgressEventCountries(inProgressEventId));
		return new ModelAndView("inProgressEventStats", modelMap);
	}
}
