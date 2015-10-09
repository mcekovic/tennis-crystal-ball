package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.util.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

@RestController
public class GOATListResource {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int PLAYER_COUNT = 1000;

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_v\n" +
		"WHERE goat_points > 0 AND goat_rank <= " + PLAYER_COUNT + "%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT player_id, goat_rank, country_id, name, goat_points, grand_slams, tour_finals, masters, olympics, big_titles, titles FROM player_v\n" +
		"WHERE goat_points > 0 AND goat_rank <= " + PLAYER_COUNT + "%1$s\n" +
		"ORDER BY %2$s, name OFFSET ? LIMIT ?";

	private static final String FILTER_SQL = " AND (name ILIKE '%' || ? || '%' OR country_id ILIKE '%' || ? || '%')";

	private static final String GOAT_POINTS_QUERY =
		"SELECT level, result, goat_points, additive FROM tournament_rank_points\n" +
		"WHERE goat_points > 0\n" +
		"ORDER BY level, result DESC";

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
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("goat_points");

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		boolean hasFilter = !isNullOrEmpty(searchPhrase);
		String filter = hasFilter ? FILTER_SQL : "";
		Object[] params = hasFilter ? new Object[] {searchPhrase, searchPhrase} : new Object[] {};

		int playerCount = Math.min(PLAYER_COUNT, jdbcTemplate.queryForObject(format(GOAT_COUNT_QUERY, filter), params, Integer.class));

		int pageSize = rowCount > 0 ? rowCount : playerCount;
		int offset = (current - 1) * pageSize;
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		params = hasFilter ? new Object[] {searchPhrase, searchPhrase, offset, pageSize} : new Object[] {offset, pageSize};

		BootgridTable<GOATListRow> table = new BootgridTable<>(current, playerCount);
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY, filter, orderBy),
			(rs) -> {
				int goatRank = rs.getInt("goat_rank");
				int playerId = rs.getInt("player_id");
				String player = rs.getString("name");
				String countryId = rs.getString("country_id");
				int goatPoints = rs.getInt("goat_points");
				GOATListRow row = new GOATListRow(goatRank, playerId, player, countryId, goatPoints);
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

	@RequestMapping("/goatPointsTable")
	public BootgridTable<GOATPointsRow> goatPointsTable() {
		BootgridTable<GOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(GOAT_POINTS_QUERY, (rs) -> {
			String level = rs.getString("level");
			String result = rs.getString("result");
			int goatPoints = rs.getInt("goat_points");
			boolean additive = rs.getBoolean("additive");
			table.addRow(new GOATPointsRow(level, result, goatPoints, additive));
		});
		return table;
	}
}
