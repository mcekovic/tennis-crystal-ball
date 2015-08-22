package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.Map.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.base.*;

import static java.lang.String.*;

@RestController
public class GOATController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int PLAYER_COUNT = 1000;

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(*) AS player_count FROM player_v " +
		"WHERE goat_rank <= " + PLAYER_COUNT + "%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT goat_rank, country_id, name, goat_points, grand_slams, tour_finals, masters, olympics, big_titles, titles FROM player_v " +
		"WHERE goat_rank <= " + PLAYER_COUNT + "%1$s " +
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
		boolean hasFilter = !Strings.isNullOrEmpty(searchPhrase);
		String filter = hasFilter ? FILTER_SQL : "";

		int playerCount = hasFilter
         ? Math.min(PLAYER_COUNT, jdbcTemplate.queryForObject(format(GOAT_COUNT_QUERY, filter), new Object[] {searchPhrase, searchPhrase}, Integer.class))
         : PLAYER_COUNT;

		int pageSize = rowCount > 0 ? rowCount : playerCount;
		int offset = (current - 1) * pageSize;
		String orderBy = getOrderBy(request);
		Object[] params = hasFilter
			? new Object[] {searchPhrase, searchPhrase, pageSize, offset}
			: new Object[] {pageSize, offset};

		BootgridTable<GOATListRow> table = new BootgridTable<>(current, playerCount);
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY, filter, orderBy),
			(rs) -> {
				int goatRank = rs.getInt("goat_rank");
				String countryId = rs.getString("country_id");
				String name = rs.getString("name");
				int goatPoints = rs.getInt("goat_points");
				GOATListRow row = new GOATListRow(goatRank, countryId, name, goatPoints);
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setBigTitles(rs.getInt("big_titles"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			params
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
		ORDER_MAP.put("bigTitles", "big_titles");
		ORDER_MAP.put("titles", "titles");
	}
}
