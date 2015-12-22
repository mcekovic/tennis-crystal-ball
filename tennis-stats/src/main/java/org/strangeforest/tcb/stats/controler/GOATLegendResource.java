package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@RestController
public class GOATLegendResource {

	@Autowired private GOATLegendService goatLegendService;


	// Tournament

	@RequestMapping("/tournamentGOATPointsTable")
	public BootgridTable<TournamentGOATPointsRow> tournamentGOATPointsTable() {
		return goatLegendService.getTournamentGOATPointsTable();
	}


	// Ranking

	@RequestMapping("/yearEndRankGOATPointsTable")
	public BootgridTable<RankGOATPointsRow> yearEndRankGOATPointsTable() {
		return goatLegendService.getYearEndRankGOATPointsTable();
	}

	@RequestMapping("/bestRankGOATPointsTable")
	public BootgridTable<RankGOATPointsRow> bestRankGOATPointsTable() {
		return goatLegendService.getBestRankGOATPointsTable();
	}

	@RequestMapping("/weeksAtNo1ForGOATPoint")
	public int weeksAtNo1ForGOATPoint() {
		return goatLegendService.getWeeksAtNo1ForGOATPoint();
	}


	// Achievements

	@RequestMapping("/bigWinRoundFactorTable")
	public BootgridTable<BigWinRoundFactorRow> bigWinRoundFactorTable() {
		return goatLegendService.getBigWinRoundFactorTable();
	}

	@RequestMapping("/bigWinRankFactorTable")
	public BootgridTable<RankGOATPointsRow> bigWinRankFactorTable() {
		return goatLegendService.getBigWinRankFactorTable();
	}

	@RequestMapping("/careerGrandSlamGOATPoints")
	public int careerGrandSlamGOATPoints() {
		return goatLegendService.getCareerGrandSlamGOATPoints();
	}

	@RequestMapping("/seasonGrandSlamGOATPoints")
	public int seasonGrandSlamGOATPoints() {
		return goatLegendService.getSeasonGrandSlamGOATPoints();
	}

	@RequestMapping("/bestSeasonGOATPointsTable")
	public BootgridTable<RankGOATPointsRow> bestSeasonGOATPointsTable() {
		return goatLegendService.getBestSeasonGOATPointsTable();
	}

	@RequestMapping("/performanceGOATPointsTable")
	public BootgridTable<PerfStatGOATPointsRow> performanceGOATPointsTable() {
		return goatLegendService.getPerformanceGOATPointsTable();
	}

	@RequestMapping("/statisticsGOATPointsTable")
	public BootgridTable<PerfStatGOATPointsRow> statisticsGOATPointsTable() {
		return goatLegendService.getStatisticsGOATPointsTable();
	}
}
