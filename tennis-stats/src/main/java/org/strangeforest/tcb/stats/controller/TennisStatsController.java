package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpHeaders.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Controller
public class TennisStatsController extends PageController {

	@Autowired private DataService dataService;
	@Autowired private TournamentForecastService forecastService;
	@Autowired private GOATListService goatListService;
	@Autowired private RankingsService rankingsService;

	private static final String FORECAST_ORDER_BY = Stream.of(InProgressEventsResource.DEFAULT_ORDER).map(OrderBy::toString).collect(joining(", "));

	@GetMapping("/")
	public ModelAndView index() {
		boolean hasInProgressEvents = forecastService.getInProgressEventsTable(FORECAST_ORDER_BY).getTotal() > 0;
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

	private static final String MAX_AGE_1_HOUR = CacheControl.maxAge(1L, TimeUnit.HOURS).cachePublic().getHeaderValue();

	@GetMapping("/rankingTopN")
	public ModelAndView rankingTopN(
      @RequestParam(name = "rankType", defaultValue = "POINTS") RankType rankType,
      @RequestParam(name = "count", defaultValue = "10") int count,
      HttpServletResponse response
	) {
		response.setHeader(CACHE_CONTROL, MAX_AGE_1_HOUR);

		LocalDate date = rankingsService.getCurrentRankingDate(rankType);
		List<PlayerRanking> rankingTopN = rankingsService.getRankingsTopN(rankType, date, count);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("count", count);
		modelMap.addAttribute("date", toDate(date));
		modelMap.addAttribute("rankingTopN", rankingTopN);
		modelMap.addAttribute("surfaces", Surface.values());
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
