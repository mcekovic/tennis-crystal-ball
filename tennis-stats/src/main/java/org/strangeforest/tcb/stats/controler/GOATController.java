package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.Map.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.strangeforest.tcb.stats.model.*;

@RestController
public class GOATController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int PLAYER_COUNT = 1000;

	private static final String GOAT_LIST_QUERY =
		"SELECT goat_ranking, name, goat_points, grand_slams, tour_finals, masters, olympics, titles FROM player_v " +
		"WHERE goat_ranking <= " + PLAYER_COUNT + " " +
		"ORDER BY %1$s, name LIMIT ? OFFSET ?";

	public static final String DEFAULT_ORDER = "goat_points DESC";

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		WebRequest request
	) {
		if (rowCount == -1)
			rowCount = PLAYER_COUNT;
		String orderBy = getOrderBy(request);
		BootgridTable<GOATListRow> table = new BootgridTable<>(current, PLAYER_COUNT);
		jdbcTemplate.query(
			String.format(GOAT_LIST_QUERY, orderBy),
			(rs) -> {
				int goatRanking = rs.getInt("goat_ranking");
				String name = rs.getString("name");
				int goatPoints = rs.getInt("goat_points");
				GOATListRow row = new GOATListRow(goatRanking, name, goatPoints);
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			rowCount, (current-1)*rowCount
		);
		return table;
	}

	private static String getOrderBy(WebRequest request) {
		String orderBy = null;
		for (Entry<String, String> order : ORDER_MAP.entrySet()) {
			String sort = findSortBy(request, order.getKey(), order.getValue());
			if (sort != null) {
				if (orderBy != null)
					orderBy += ", " + sort;
				else
					orderBy = sort;
			}
		}
		if (orderBy != null) {
			if (!orderBy.contains(DEFAULT_ORDER))
				orderBy += ", " + DEFAULT_ORDER;
			return orderBy;
		}
		else
			return DEFAULT_ORDER;
	}

	private static String findSortBy(WebRequest request, String attrName, final String columnName) {
		String sort = request.getParameter("sort[" + attrName + "]");
		return sort != null ? columnName + " " + sort.toUpperCase() : null;
	}

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("goatPoints", "goat_points");
		ORDER_MAP.put("grandSlams", "grand_slams");
		ORDER_MAP.put("tourFinals", "tour_finals");
		ORDER_MAP.put("masters", "masters");
		ORDER_MAP.put("olympics", "olympics");
		ORDER_MAP.put("titles", "titles");
	}
}
