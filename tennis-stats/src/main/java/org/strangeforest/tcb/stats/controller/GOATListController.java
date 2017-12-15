package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;
import static org.strangeforest.tcb.stats.model.GOATListConfig.*;

@Controller
public class GOATListController extends PageController {

	@Autowired private GOATLegendService goatLegendService;

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
		@RequestParam(name = "levelFactors", defaultValue = "") String levelFactors,
		@RequestParam(name = "resultFactors", defaultValue = "") String resultFactors,
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
		GOATListConfig config = new GOATListConfig(
			oldLegends, extrapolate, tournamentFactor, rankingFactor, achievementsFactor, parseIntProperties(levelFactors), parseIntProperties(resultFactors),
			yearEndRankFactor, bestRankFactor, weeksAtNo1Factor, weeksAtEloTopNFactor, bestEloRatingFactor,
			grandSlamFactor, bigWinsFactor, h2hFactor, recordsFactor, bestSeasonFactor, greatestRivalriesFactor, performanceFactor, statisticsFactor
		);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("config", config);
		modelMap.addAttribute("factors", FACTOR_MAP);
		modelMap.addAttribute("levels", TOURNAMENT_LEVELS);
		modelMap.addAttribute("results", TOURNAMENT_RESULTS);

		return new ModelAndView("goatList", modelMap);
	}

	@GetMapping("/goatLegend")
	public ModelAndView goatLegend(
		@RequestParam(name = "forSeason", required = false) boolean forSeason,
		@RequestParam(name = "tournamentFactor", defaultValue = "1") int tournamentFactor,
		@RequestParam(name = "rankingFactor", defaultValue = "1") int rankingFactor,
		@RequestParam(name = "achievementsFactor", defaultValue = "1") int achievementsFactor,
		@RequestParam(name = "levelFactors", defaultValue = "") String levelFactors,
		@RequestParam(name = "resultFactors", defaultValue = "") String resultFactors,
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
		GOATListConfig config = new GOATListConfig(
			true, false, tournamentFactor, rankingFactor, achievementsFactor, parseIntProperties(levelFactors), parseIntProperties(resultFactors),
			yearEndRankFactor, bestRankFactor, weeksAtNo1Factor, weeksAtEloTopNFactor, bestEloRatingFactor,
			grandSlamFactor, bigWinsFactor, h2hFactor, recordsFactor, bestSeasonFactor, greatestRivalriesFactor, performanceFactor, statisticsFactor
		);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("forSeason", forSeason);
		// Tournament
		modelMap.addAttribute("tournamentGOATPoints", applyConfig(goatLegendService.getTournamentGOATPoints(), config));
		// Ranking
		modelMap.addAttribute("yearEndRankGOATPoints", applyRankFactor(goatLegendService.getYearEndRankGOATPoints(), config.getYearEndRankTotalFactor()));
		modelMap.addAttribute("bestRankGOATPoints", applyRankFactor(goatLegendService.getBestRankGOATPoints(), config.getBestRankTotalFactor()));
		modelMap.addAttribute("weeksAtNo1PointFactor", config.getWeeksAtNo1TotalFactor());
		modelMap.addAttribute("weeksAtNo1ForGOATPoint", goatLegendService.getWeeksAtNo1ForGOATPoint());
		modelMap.addAttribute("weeksAtEloTopNPointFactor", config.getWeeksAtEloTopNTotalFactor());
		modelMap.addAttribute("weeksAtEloTopNForGOATPoint", goatLegendService.getWeeksAtEloTopNGOATPoint());
		int bestEloRatingTotalFactor = config.getBestEloRatingTotalFactor();
		modelMap.addAttribute("bestEloRatingGOATPoints", applyRankFactor(goatLegendService.getBestEloRatingGOATPoints(), bestEloRatingTotalFactor));
		modelMap.addAttribute("bestSurfaceEloRatingGOATPoints", applyRankFactor(goatLegendService.getBestSurfaceEloRatingGOATPoints(), bestEloRatingTotalFactor));
		modelMap.addAttribute("bestIndoorEloRatingGOATPoints", applyRankFactor(goatLegendService.getBestIndoorEloRatingGOATPoints(), bestEloRatingTotalFactor));
		// Achievements
		int grandSlamTotalFactor = config.getGrandSlamTotalFactor();
		modelMap.addAttribute("careerGrandSlamGOATPoints", goatLegendService.getCareerGrandSlamGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("seasonGrandSlamGOATPoints", goatLegendService.getSeasonGrandSlamGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("season3GrandSlamGOATPoints", goatLegendService.getSeason3GrandSlamGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("grandSlamHolderGOATPoints", goatLegendService.getGrandSlamHolderGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("consecutiveGrandSlamOnSameEventGOATPoints", goatLegendService.getConsecutiveGrandSlamOnSameEventGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("grandSlamOnSameEventGOATPoints", goatLegendService.getGrandSlamOnSameEventGOATPoints() * grandSlamTotalFactor);
		modelMap.addAttribute("bigWinMatchFactors", applyBigWinsFactor(goatLegendService.getBigWinMatchFactors(), config.getBigWinsTotalFactor()));
		modelMap.addAttribute("bigWinRankFactors", goatLegendService.getBigWinRankFactors());
		modelMap.addAttribute("h2hRankFactors", goatLegendService.getH2hRankFactors());
		modelMap.addAttribute("h2hPointFactor", config.getH2hTotalFactor());
		modelMap.addAttribute("bestSeasonGOATPoints", applyRankFactor(goatLegendService.getBestSeasonGOATPoints(), config.getBestSeasonTotalFactor()));
		modelMap.addAttribute("greatestRivalriesGOATPoints", applyRankFactor(goatLegendService.getGreatestRivalriesGOATPoints(), config.getGreatestRivalriesTotalFactor()));
		return new ModelAndView("goatLegend", modelMap);
	}

	@GetMapping("/recordsGOATPointsLegend")
	public ModelAndView recordsGOATPointsLegend(
		@RequestParam(name = "factor", defaultValue = "1") int factor
	) {
		return new ModelAndView("recordsGOATPointsLegend", "recordsGOATPoints", applyRecordsFactor(goatLegendService.getRecordsGOATPoints(), factor));
	}

	@GetMapping("/performanceGOATPointsLegend")
	public ModelAndView performanceGOATPointsLegend(
		@RequestParam(name = "factor", defaultValue = "1") int factor
	) {
		return new ModelAndView("performanceGOATPointsLegend", "performanceGOATPoints", applyPerfStatFactor(goatLegendService.getPerformanceGOATPoints(), factor));
	}

	@GetMapping("/statisticsGOATPointsLegend")
	public ModelAndView statisticsGOATPointsLegend(
		@RequestParam(name = "factor", defaultValue = "1") int factor
	) {
		return new ModelAndView("statisticsGOATPointsLegend", "statisticsGOATPoints", applyPerfStatFactor(goatLegendService.getStatisticsGOATPoints(), factor));
	}

	private static List<TournamentGOATPoints> applyConfig(List<TournamentGOATPoints> tournamentPoints, GOATListConfig config) {
		for (TournamentGOATPoints point : tournamentPoints)
			point.applyConfig(config);
		return tournamentPoints;
	}

	private static List<RankGOATPoints> applyRankFactor(List<RankGOATPoints> rankPoints, int factor) {
		for (RankGOATPoints point : rankPoints)
			point.applyFactor(factor);
		return rankPoints;
	}

	private static List<BigWinMatchFactor> applyBigWinsFactor(List<BigWinMatchFactor> bigWinMatchFactors, int factor) {
		for (BigWinMatchFactor point : bigWinMatchFactors)
			point.applyFactor(factor);
		return bigWinMatchFactors;
	}

	private static Map<String, Map<Record, String>> applyRecordsFactor(Map<String, Map<Record, String>> goatPoints, int factor) {
		for (Map<Record, String> map : goatPoints.values()) {
			for (Map.Entry<Record, String> entry : map.entrySet())
				entry.setValue(applyFactorToCSV(entry.getValue(), factor));
		}
		return goatPoints;
	}

	private static List<PerfStatGOATPoints> applyPerfStatFactor(List<PerfStatGOATPoints> perfStatPoints, int factor) {
		for (PerfStatGOATPoints point : perfStatPoints)
			point.applyFactor(factor);
		return perfStatPoints;
	}
}
