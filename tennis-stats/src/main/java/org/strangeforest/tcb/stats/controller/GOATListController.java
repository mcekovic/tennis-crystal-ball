package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;

@Controller
public class GOATListController extends PageController {

	private static Map<Integer, String> FACTOR_MAP = ImmutableMap.<Integer, String>builder()
		.put(0, "x 0")
		.put(1, "x 1")
		.put(2, "x 2")
		.put(3, "x 3")
		.put(4, "x 4")
		.put(5, "x 5")
		.put(6, "x 6")
		.put(8, "x 8")
		.put(10, "x 10")
	.build();

	@GetMapping("/goatList")
	public ModelAndView goatList(
		@RequestParam(name = "oldLegends", defaultValue = T) boolean oldLegends,
		@RequestParam(name = "extrapolate", defaultValue = F) boolean extrapolate,
		@RequestParam(name = "tournamentPointsFactor", defaultValue = "1") int tournamentPointsFactor,
		@RequestParam(name = "rankingPointsFactor", defaultValue = "1") int rankingPointsFactor,
		@RequestParam(name = "achievementsPointsFactor", defaultValue = "1") int achievementsPointsFactor,
		@RequestParam(name = "yearEndRankPointsFactor", defaultValue = "1") int yearEndRankPointsFactor,
		@RequestParam(name = "bestRankPointsFactor", defaultValue = "1") int bestRankPointsFactor,
		@RequestParam(name = "weeksAtNo1PointsFactor", defaultValue = "1") int weeksAtNo1PointsFactor,
		@RequestParam(name = "weeksAtEloTopNPointsFactor", defaultValue = "1") int weeksAtEloTopNPointsFactor,
		@RequestParam(name = "bestEloRatingPointsFactor", defaultValue = "1") int bestEloRatingPointsFactor,
		@RequestParam(name = "grandSlamPointsFactor", defaultValue = "1") int grandSlamPointsFactor,
		@RequestParam(name = "bigWinsPointsFactor", defaultValue = "1") int bigWinsPointsFactor,
		@RequestParam(name = "h2hPointsFactor", defaultValue = "1") int h2hPointsFactor,
		@RequestParam(name = "recordsPointsFactor", defaultValue = "1") int recordsPointsFactor,
		@RequestParam(name = "bestSeasonPointsFactor", defaultValue = "1") int bestSeasonPointsFactor,
		@RequestParam(name = "greatestRivalriesPointsFactor", defaultValue = "1") int greatestRivalriesPointsFactor,
		@RequestParam(name = "performancePointsFactor", defaultValue = "1") int performancePointsFactor,
		@RequestParam(name = "statisticsPointsFactor", defaultValue = "1") int statisticsPointsFactor
	) {
		GOATListConfig config = new GOATListConfig(
			oldLegends, extrapolate, tournamentPointsFactor, rankingPointsFactor, achievementsPointsFactor,
			yearEndRankPointsFactor, bestRankPointsFactor, weeksAtNo1PointsFactor, weeksAtEloTopNPointsFactor, bestEloRatingPointsFactor,
			grandSlamPointsFactor, bigWinsPointsFactor, h2hPointsFactor, recordsPointsFactor, bestSeasonPointsFactor, greatestRivalriesPointsFactor, performancePointsFactor, statisticsPointsFactor
		);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("config", config);
		modelMap.addAttribute("factors", FACTOR_MAP);

		return new ModelAndView("goatList", modelMap);
	}
}
