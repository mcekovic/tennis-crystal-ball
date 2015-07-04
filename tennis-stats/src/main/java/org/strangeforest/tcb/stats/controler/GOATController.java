package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.Map.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.base.*;

@RestController
public class GOATController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int PLAYER_COUNT = 1000;

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT goat_ranking, country_id, name, goat_points, grand_slams, tour_finals, masters, olympics, titles FROM player_v " +
		"WHERE goat_ranking <= " + PLAYER_COUNT + "%1$s " +
		"ORDER BY %2$s, name LIMIT ? OFFSET ?";

	public static final String FILTER_SQL = " AND (name ILIKE '%' || ? || '%' OR country_id ILIKE '%' || ? || '%')";
	public static final String DEFAULT_ORDER = "goat_points DESC";

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		WebRequest request
	) {
		int pageSize = getPageSize(rowCount);
		int offset = (current - 1) * pageSize;
		String filter = getFilter(searchPhrase);
		String orderBy = getOrderBy(request);
		Object[] params = filter.length() == 0
			? new Object[] {pageSize, offset}
			: new Object[] {searchPhrase, searchPhrase, pageSize, offset};
		BootgridTable<GOATListRow> table = new BootgridTable<>(current, PLAYER_COUNT);
		jdbcTemplate.query(
			String.format(GOAT_LIST_QUERY, filter, orderBy),
			(rs) -> {
				int goatRanking = rs.getInt("goat_ranking");
				String countryId = rs.getString("country_id");
				String name = rs.getString("name");
				int goatPoints = rs.getInt("goat_points");
				GOATListRow row = new GOATListRow(goatRanking, countryId, name, goatPoints);
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			params
		);
		return table;
	}

	private int getPageSize(int rowCount) {
		return rowCount > 0 ? rowCount : PLAYER_COUNT;
	}

	private String getFilter(String searchPhrase) {
		return Strings.isNullOrEmpty(searchPhrase) ? "" : FILTER_SQL;
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
