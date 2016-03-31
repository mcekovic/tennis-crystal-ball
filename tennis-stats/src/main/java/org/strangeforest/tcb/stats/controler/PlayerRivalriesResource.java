package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import static org.strangeforest.tcb.stats.util.OrderBy.*;

@RestController
public class PlayerRivalriesResource {

	@Autowired private RivalriesService rivalriesService;

	private static final int MAX_RIVALRIES = 1000;

	private static Map<String, String> ORDER_MAP = new TreeMap<String, String>() {{
		put("bestRank", "best_rank");
		put("matches", "matches");
		put("won", "won");
		put("lost", "lost");
		put("wonPctStr", "won::real/(won + lost)");
	}};
	private static final OrderBy[] DEFAULT_ORDERS = new OrderBy[] {desc("matches"), desc("won")};

	@RequestMapping("/playerRivalriesTable")
	public BootgridTable<PlayerRivalryRow> playerRivalriesTable(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		PlayerListFilter filter = new PlayerListFilter(searchPhrase);
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDERS);
		int pageSize = rowCount > 0 ? rowCount : MAX_RIVALRIES;
		return rivalriesService.getPlayerRivalriesTable(playerId, filter, orderBy, pageSize, current);
	}
}
