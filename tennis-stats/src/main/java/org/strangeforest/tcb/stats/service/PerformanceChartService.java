package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class PerformanceChartService {

	@Autowired private PlayerService playerService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_SEASON_PERFORMANCE_QUERY = //language=SQL
		"SELECT %1$s, player_id, CASE WHEN %2$s_won + %2$s_lost > 0 THEN %2$s_won::REAL / (%2$s_won + %2$s_lost) ELSE NULL END AS won_lost_pct\n" +
		"FROM player_season_performance pf%3$s\n" +
		"WHERE player_id IN (:playerIds)%4$s\n" +
		"ORDER BY %5$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getPerformanceDataTable(int[] playerIds, PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerIds);
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
		if (players.isEmpty())
			return table;
		RowCursor rowCursor = new IntegerRowCursor(table, players);
		jdbcTemplate.query(
			getSQL(category, seasonRange, byAge),
			getParams(players, seasonRange),
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

	private String getSQL(PerformanceCategory category, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_PERFORMANCE_QUERY,
			byAge ? "extract(YEAR FROM age((pf.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "pf.season",
			category.getColumn(),
			byAge ? PLAYER_JOIN : "",
			rangeFilter(seasonRange, "pf.season", "season"),
			byAge ? "age" : "season"
		);
	}

	private MapSqlParameterSource getParams(IndexedPlayers players, Range<Integer> seasonRange) {
		MapSqlParameterSource params = params("playerIds", players.getPlayerIds());
		addRangeParams(params, seasonRange, "season");
		return params;
	}
}
