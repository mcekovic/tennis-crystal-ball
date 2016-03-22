package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class PerformanceChartService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_SEASON_PERFORMANCE_QUERY = //language=SQL
		"SELECT %1$s, player_id, CASE WHEN %2$s_won + %2$s_lost > 0 THEN %2$s_won::real/(%2$s_won + %2$s_lost) ELSE NULL END AS won_lost_pct\n" +
		"FROM player_season_performance pf%3$s\n" +
		"WHERE player_id = %4$s%5$s\n" +
		"ORDER BY %6$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getPerformanceDataTable(int playerId, PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerId);
		DataTable table = fetchPerformanceDataTable(indexedPlayers, category, seasonRange, byAge);
		addColumns(table, indexedPlayers, category, byAge);
		return table;
	}

	public DataTable getPerformanceDataTable(List<String> players, PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchPerformanceDataTable(indexedPlayers, category, seasonRange, byAge);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, category, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "Players" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchPerformanceDataTable(IndexedPlayers players, PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		DataTable table = new DataTable();
		RowCursor rowCursor = new IntegerRowCursor(table, players);
		jdbcTemplate.query(
			getSQL(players.getCount(), category, seasonRange, byAge),
			ps -> {
				int index = 1;
				if (players.getCount() == 1)
					ps.setInt(index, players.getPlayerIds().iterator().next());
				else
					bindIntegerArray(ps, index, players.getPlayerIds());
				index = bindIntegerRange(ps, index, seasonRange);
			},
			rs -> {
				Object x = byAge ? rs.getInt("age") : rs.getInt("season");
				int playerId = rs.getInt("player_id");
				double y = round(rs.getDouble("won_lost_pct"), 10000.0);
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static double round(double value, double by) {
		return Math.round(value * by) / by;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, PerformanceCategory category, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else
			table.addColumn("number", "Season");
		for (String player : players.getPlayers())
			table.addColumn("number", player + " " + category.getTitle());
	}

	private String getSQL(int playerCount, PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_PERFORMANCE_QUERY,
			byAge ? "date_part('year', age((pf.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "pf.season",
			category.getColumn(),
			byAge ? PLAYER_JOIN : "",
			playerCount == 1 ? "?" : "ANY(?)",
			conditions(seasonRange, "pf.season"),
			byAge ? "age" : "season"
		);
	}

	private String conditions(Range<?> range, String column) {
		StringBuilder conditions = new StringBuilder();
		if (range.hasLowerBound())
			conditions.append(" AND ").append(column).append(" >= ?");
		if (range.hasUpperBound())
			conditions.append(" AND ").append(column).append(" <= ?");
		return conditions.toString();
	}
}
