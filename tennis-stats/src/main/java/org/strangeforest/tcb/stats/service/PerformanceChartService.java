package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class PerformanceChartService {

	@Autowired private PlayerService playerService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	public enum PerformanceChartType {
		WON_LOST_PCT("Winning %", "CASE WHEN %1$s_won + %1$s_lost > 0 THEN %1$s_won::REAL / (%1$s_won + %1$s_lost) END"),
		WON("Won", "%1$s_won"),
		LOST("Lost", "%1$s_lost"),
		PLAYED("Played", "%1$s_won + %1$s_lost");

		private final String text;
		private final String expression;

		PerformanceChartType(String text, String expression) {
			this.text = text;
			this.expression = expression;
		}

		public String getText() {
			return text;
		}

		public String getExpression() {
			return expression;
		}
	}

	private static final String PLAYER_SEASON_PERFORMANCE_QUERY = //language=SQL
		"SELECT %1$s, player_id, %2$s AS value\n" +
		"FROM player_season_performance pf%3$s\n" +
		"WHERE player_id IN (:playerIds)%4$s\n" +
		"ORDER BY %5$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getPerformanceDataTable(int[] playerIds, PerformanceCategory category, PerformanceChartType chartType, Range<Integer> seasonRange, boolean byAge) {
		var indexedPlayers = playerService.getIndexedPlayers(playerIds);
		var table = fetchPerformanceDataTable(indexedPlayers, category, chartType, seasonRange, byAge);
		addColumns(table, indexedPlayers, category, byAge);
		return table;
	}

	public DataTable getPerformanceDataTable(List<String> players, PerformanceCategory category, PerformanceChartType chartType, Range<Integer> seasonRange, boolean byAge) {
		var indexedPlayers = playerService.getIndexedPlayers(players);
		var table = fetchPerformanceDataTable(indexedPlayers, category, chartType, seasonRange, byAge);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, category, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "Players" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchPerformanceDataTable(IndexedPlayers players, PerformanceCategory category, PerformanceChartType chartType, Range<Integer> seasonRange, boolean byAge) {
		var table = new DataTable();
		if (players.isEmpty())
			return table;
		RowCursor rowCursor = new IntegerRowCursor(table, players);
		jdbcTemplate.query(
			getSQL(category, chartType, seasonRange, byAge),
			getParams(players, seasonRange),
			rs -> {
				Object x = byAge ? rs.getInt("age") : rs.getInt("season");
				var playerId = rs.getInt("player_id");
				var y = getValue(rs, chartType);
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, PerformanceCategory category, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else
			table.addColumn("number", "Season");
		for (var player : players.getPlayers())
			table.addColumn("number", player + " " + category.getTitle());
	}

	private String getSQL(PerformanceCategory category, PerformanceChartType chartType, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_PERFORMANCE_QUERY,
			byAge ? "extract(YEAR FROM age(make_date(pf.season, 12, 31), p.dob)) AS age" : "pf.season",
			format(chartType.getExpression(), category.getColumn()),
			byAge ? PLAYER_JOIN : "",
			rangeFilter(seasonRange, "pf.season", "season"),
			byAge ? "age" : "season"
		);
	}

	private MapSqlParameterSource getParams(IndexedPlayers players, Range<Integer> seasonRange) {
		var params = params("playerIds", players.getPlayerIds());
		addRangeParams(params, seasonRange, "season");
		return params;
	}

	private static Number getValue(ResultSet rs, PerformanceChartType chartType) throws SQLException {
		switch (chartType) {
			case WON_LOST_PCT: return round(getDouble(rs, "value"), 10000.0);
			default: return getInteger(rs, "value");
		}
	}

	private static Double round(Double value, double by) {
		return value != null ? Math.round(value * by) / by : null;
	}
}
