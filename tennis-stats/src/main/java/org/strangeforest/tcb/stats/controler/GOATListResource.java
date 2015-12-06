package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class GOATListResource {

	@Autowired private GOATListService goatListService;

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("goatPoints", "goat_points");
		ORDER_MAP.put("tournamentGoatPoints", "tournament_goat_points");
		ORDER_MAP.put("rankingGoatPoints", "ranking_goat_points");
		ORDER_MAP.put("performanceGoatPoints", "performance_goat_points");
		ORDER_MAP.put("grandSlams", "grand_slams");
		ORDER_MAP.put("tourFinals", "tour_finals");
		ORDER_MAP.put("masters", "masters");
		ORDER_MAP.put("olympics", "olympics");
		ORDER_MAP.put("bigTitles", "big_titles");
		ORDER_MAP.put("titles", "titles");
	}
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("goat_points"), asc("name")};

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		int playerCount = goatListService.getPlayerCount(filter);

		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : playerCount;
		return goatListService.getGOATListTable(playerCount, filter, orderBy, pageSize, current);
	}

	@RequestMapping("/tournamentGOATPointsTable")
	public BootgridTable<TournamentGOATPointsRow> tournamentGOATPointsTable() {
		return goatListService.getTournamentGOATPointsTable();
	}

	@RequestMapping("/yearEndRankGOATPointsTable")
	public BootgridTable<RankGOATPointsRow> yearEndRankGOATPointsTable() {
		return goatListService.getYearEndRankGOATPointsTable();
	}

	@RequestMapping("/bestRankGOATPointsTable")
	public BootgridTable<RankGOATPointsRow> bestRankGOATPointsTable() {
		return goatListService.getBestRankGOATPointsTable();
	}

	@RequestMapping("/performanceGOATPointsTable")
	public BootgridTable<PerfStatGOATPointsRow> performanceGOATPointsTable() {
		return goatListService.getPerformanceGOATPointsTable();
	}

	@RequestMapping("/statisticsGOATPointsTable")
	public BootgridTable<PerfStatGOATPointsRow> statisticsGOATPointsTable() {
		return goatListService.getStatisticsGOATPointsTable();
	}
}
