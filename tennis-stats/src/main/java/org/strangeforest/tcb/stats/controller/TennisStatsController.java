package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class TennisStatsController extends PageController {

	@Autowired private DataService dataService;
	@Autowired private TournamentForecastService forecastService;
	@Autowired private GOATListService goatListService;
	@Autowired private RankingsService rankingsService;

	@GetMapping("/")
	public ModelAndView index() {
		boolean hasInProgressEvents = forecastService.getInProgressEventsTable().getTotal() > 0;
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("hasInProgressEvents", hasInProgressEvents);
		modelMap.addAttribute("currentSeason", dataService.getLastSeason());
		modelMap.addAttribute("goatTopN", goatTopN);
		return new ModelAndView("index", modelMap);
	}

	@GetMapping("/about")
	public ModelAndView about() {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);
		return new ModelAndView("about", "goatTopN", goatTopN);
	}

	private static final String ELO_RATING_SUFFIX = "_ELO_RATING";

	@GetMapping("/rankingTopN")
	public ModelAndView rankingTopN(
      @RequestParam(name = "rankType", defaultValue = "POINTS") RankType rankType,
      @RequestParam(name = "count", defaultValue = "10") int count
	) {
		LocalDate date = rankingsService.getCurrentRankingDate(rankType);
		List<PlayerRanking> rankingTopN = rankingsService.getRankingsTopN(rankType, date, count);
		String rankTypeName = rankType.name();
		Surface surface = rankTypeName.endsWith(ELO_RATING_SUFFIX) ? Surface.valueOf(rankTypeName.substring(0, rankTypeName.length() - ELO_RATING_SUFFIX.length())) : null;

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("count", count);
		modelMap.addAttribute("date", toDate(date));
		modelMap.addAttribute("rankingTopN", rankingTopN);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("surface", surface);
		return new ModelAndView("rankingTopN", modelMap);
	}

	@GetMapping("/goatList")
	public String goatList() {
		return "goatList";
	}

	@GetMapping("/liveScores")
	public String liveScores() {
		return "liveScores";
	}

	@Value("${tennis-stats.down-for-maintenance:false}")
	private boolean downForMaintenance;

	@Value("${tennis-stats.down-for-maintenance.message:}")
	private String maintenanceMessage;

	@GetMapping("/maintenance")
	public ModelAndView maintenance() {
		return downForMaintenance
			? new ModelAndView("maintenance", "maintenanceMessage", maintenanceMessage)
			: new ModelAndView("index");
	}
}
