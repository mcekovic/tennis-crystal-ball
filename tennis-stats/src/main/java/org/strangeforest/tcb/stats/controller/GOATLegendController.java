 package org.strangeforest.tcb.stats.controller;

 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.*;
 import org.springframework.ui.*;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.*;
 import org.strangeforest.tcb.stats.service.*;

@Controller
public class GOATLegendController extends BaseController {

	@Autowired private GOATLegendService goatLegendService;

	@GetMapping("/goatLegend")
	public ModelAndView goatLegend(
		@RequestParam(name = "forSeason", required = false) boolean forSeason
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("forSeason", forSeason);
		// Tournament
		modelMap.addAttribute("tournamentGOATPoints", goatLegendService.getTournamentGOATPoints());
		// Ranking
		modelMap.addAttribute("yearEndRankGOATPoints", goatLegendService.getYearEndRankGOATPoints());
		modelMap.addAttribute("bestRankGOATPoints", goatLegendService.getBestRankGOATPoints());
		modelMap.addAttribute("weeksAtNo1ForGOATPoint", goatLegendService.getWeeksAtNo1ForGOATPoint());
		modelMap.addAttribute("weeksAtEloTopNForGOATPoint", goatLegendService.getWeeksAtEloTopNGOATPoint());
		modelMap.addAttribute("bestEloRatingGOATPoints", goatLegendService.getBestEloRatingGOATPoints());
		modelMap.addAttribute("bestSurfaceEloRatingGOATPoints", goatLegendService.getBestSurfaceEloRatingGOATPoints());
		// Achievements
		modelMap.addAttribute("bigWinMatchFactors", goatLegendService.getBigWinMatchFactors());
		modelMap.addAttribute("bigWinRankFactors", goatLegendService.getBigWinRankFactors());
		modelMap.addAttribute("h2hRankFactors", goatLegendService.getH2hRankFactors());
		modelMap.addAttribute("careerGrandSlamGOATPoints", goatLegendService.getCareerGrandSlamGOATPoints());
		modelMap.addAttribute("seasonGrandSlamGOATPoints", goatLegendService.getSeasonGrandSlamGOATPoints());
		modelMap.addAttribute("grandSlamHolderGOATPoints", goatLegendService.getGrandSlamHolderGOATPoints());
		modelMap.addAttribute("bestSeasonGOATPoints", goatLegendService.getBestSeasonGOATPoints());
		modelMap.addAttribute("greatestRivalriesGOATPoints", goatLegendService.getGreatestRivalriesGOATPoints());
		modelMap.addAttribute("performanceGOATPoints", goatLegendService.getPerformanceGOATPoints());
		modelMap.addAttribute("statisticsGOATPoints", goatLegendService.getStatisticsGOATPoints());
		return new ModelAndView("goatLegend", modelMap);
	}
}