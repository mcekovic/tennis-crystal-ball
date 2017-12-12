package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;

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
		@RequestParam(name = "tournamentFactor", defaultValue = "1") int tournamentFactor,
		@RequestParam(name = "rankingFactor", defaultValue = "1") int rankingFactor,
		@RequestParam(name = "achievementsFactor", defaultValue = "1") int achievementsFactor,
		@RequestParam(name = "levelGFactor", defaultValue = "1") int levelGFactor,
		@RequestParam(name = "levelFFactor", defaultValue = "1") int levelFFactor,
		@RequestParam(name = "levelLFactor", defaultValue = "1") int levelLFactor,
		@RequestParam(name = "levelMFactor", defaultValue = "1") int levelMFactor,
		@RequestParam(name = "levelOFactor", defaultValue = "1") int levelOFactor,
		@RequestParam(name = "levelAFactor", defaultValue = "1") int levelAFactor,
		@RequestParam(name = "levelBFactor", defaultValue = "1") int levelBFactor,
		@RequestParam(name = "levelDFactor", defaultValue = "1") int levelDFactor,
		@RequestParam(name = "levelTFactor", defaultValue = "1") int levelTFactor,
		@RequestParam(name = "yearEndRankFactor", defaultValue = "1") int yearEndRankFactor,
		@RequestParam(name = "bestRankFactor", defaultValue = "1") int bestRankFactor,
		@RequestParam(name = "weeksAtNo1Factor", defaultValue = "1") int weeksAtNo1Factor,
		@RequestParam(name = "weeksAtEloTopNFactor", defaultValue = "1") int weeksAtEloTopNFactor,
		@RequestParam(name = "bestEloRatingFactor", defaultValue = "1") int bestEloRatingFactor,
		@RequestParam(name = "grandSlamFactor", defaultValue = "1") int grandSlamFactor,
		@RequestParam(name = "bigWinsFactor", defaultValue = "1") int bigWinsFactor,
		@RequestParam(name = "h2hFactor", defaultValue = "1") int h2hFactor,
		@RequestParam(name = "recordsFactor", defaultValue = "1") int recordsFactor,
		@RequestParam(name = "bestSeasonFactor", defaultValue = "1") int bestSeasonFactor,
		@RequestParam(name = "greatestRivalriesFactor", defaultValue = "1") int greatestRivalriesFactor,
		@RequestParam(name = "performanceFactor", defaultValue = "1") int performanceFactor,
		@RequestParam(name = "statisticsFactor", defaultValue = "1") int statisticsFactor
	) {
		Map<String, Integer> levelFactors = ImmutableMap.<String, Integer>builder()
			.put("G", levelGFactor)
			.put("F", levelFFactor)
			.put("L", levelLFactor)
			.put("M", levelMFactor)
			.put("O", levelOFactor)
			.put("A", levelAFactor)
			.put("B", levelBFactor)
			.put("D", levelDFactor)
			.put("T", levelTFactor)
		.build();
		GOATListConfig config = new GOATListConfig(
			oldLegends, extrapolate, tournamentFactor, rankingFactor, achievementsFactor, levelFactors,
			yearEndRankFactor, bestRankFactor, weeksAtNo1Factor, weeksAtEloTopNFactor, bestEloRatingFactor,
			grandSlamFactor, bigWinsFactor, h2hFactor, recordsFactor, bestSeasonFactor, greatestRivalriesFactor, performanceFactor, statisticsFactor
		);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("config", config);
		modelMap.addAttribute("factors", FACTOR_MAP);
		modelMap.addAttribute("levels", GOATListConfig.TOURNAMENT_LEVELS);
		modelMap.addAttribute("levelEnum", TournamentLevel.GRAND_SLAM);

		return new ModelAndView("goatList", modelMap);
	}
}
