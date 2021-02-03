package org.strangeforest.tcb.stats.controller;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.model.core.RankCategory.*;
import static org.strangeforest.tcb.util.DateUtil.*;
import static org.thymeleaf.util.StringUtils.*;

@Controller
public class RankingsController extends PageController {

	public static final List<Integer> REFERENCE_RANKS = List.of(1, 2, 3, 4, 5, 10, 20, 50, 100);
	
	@Autowired private PlayerService playerService;
	@Autowired private RankingsService rankingsService;

	@GetMapping("/rankingsTable")
	public ModelAndView rankingsTable(
		@RequestParam(name = "rankType", defaultValue = "RANK") RankType rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date
	) {
		return showRankingsTable(rankType, season, date, false);
	}

	@GetMapping("/eloRatings")
	public ModelAndView eloRatings(
		@RequestParam(name = "rankType", defaultValue = "ELO_RANK") RankType rankType,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date
	) {
		return showRankingsTable(rankType, season, date, true);
	}

	private ModelAndView showRankingsTable(RankType rankType, Integer season, LocalDate date, boolean eloOnly) {
		if (date != null)
			season = date.getYear();
		var modelMap = new ModelMap();
		modelMap.addAttribute("rankCategories", eloOnly ? EnumSet.of(ELO) : EnumSet.of(ATP, ELO));
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("tableDate", rankingsService.getRankingsDate(rankType, season, date));
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("season", season);
		modelMap.addAttribute("date", date);
		if (season != null)
			modelMap.addAttribute("dates", rankingsService.getSeasonRankingDates(rankType, season));
		modelMap.addAttribute("eloOnly", eloOnly);
		return new ModelAndView("rankingsTable", modelMap);
	}

	@GetMapping("/peakEloRatings")
	public ModelAndView peakEloRatings(
		@RequestParam(name = "rankType", defaultValue = "ELO_RATING") RankType rankType
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("rankTypes", ELO.getRankTypes());
		modelMap.addAttribute("rankType", rankType);
		return new ModelAndView("peakEloRatings", modelMap);
	}

	@GetMapping("/rankingsChart")
	public ModelAndView rankingsChart(
		@RequestParam(name = "players", required = false) String players,
		@RequestParam(name = "refRank", required = false) Integer refRank,
		@RequestParam(name = "rankType", required = false) RankType rankType,
		@RequestParam(name = "compensatePoints", defaultValue = F) Boolean compensatePoints,
		@RequestParam(name = "timeSpan", required = false) String timeSpan,
		@RequestParam(name = "bySeason", defaultValue = F) Boolean bySeason,
		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate fromDate,
		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate toDate,
		@RequestParam(name = "season", required = false) Integer season,
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "byAge", defaultValue = F) Boolean byAge
	) {
		var modelMap = new ModelMap();
		modelMap.addAttribute("players", players);
		modelMap.addAttribute("refRank", refRank);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("compensatePoints", compensatePoints);
		if (isEmpty(timeSpan) && (fromDate != null || toDate != null || season != null || fromSeason != null || toSeason != null))
			timeSpan = RankingsResource.CUSTOM;
		modelMap.addAttribute("timeSpan", timeSpan);
		modelMap.addAttribute("bySeason", bySeason);
		modelMap.addAttribute("fromDate", fromDate);
		modelMap.addAttribute("toDate", toDate);
		modelMap.addAttribute("fromSeason", fromSeason);
		modelMap.addAttribute("toSeason", toSeason);
		if (season != null) {
			if (fromDate == null)
				modelMap.addAttribute("fromDate", LocalDate.of(season, 1, 1));
			if (toDate == null)
				modelMap.addAttribute("toDate", LocalDate.of(season, 12, 31));
		}
		modelMap.addAttribute("byAge", byAge);
		modelMap.addAttribute("playerQuickPicks", playerService.getPlayerQuickPicks());
		modelMap.addAttribute("rankCategories", RankCategory.values());
		modelMap.addAttribute("referenceRanks", REFERENCE_RANKS);
		modelMap.addAttribute("seasons", dataService.getSeasons());
		return new ModelAndView("rankingsChart", modelMap);
	}
}
