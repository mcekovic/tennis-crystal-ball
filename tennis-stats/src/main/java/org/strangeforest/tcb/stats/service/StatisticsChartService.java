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
public class StatisticsChartService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_SEASON_STATISTICS_QUERY = //language=SQL
		"SELECT %1$s, player_id, %2$s AS value\n" +
		"FROM player_season_stats s%3$s\n" +
		"WHERE player_id = %4$s%5$s\n" +
		"ORDER BY %6$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getStatisticsDataTable(int playerId, StatsCategory category, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerId);
		DataTable table = fetchStatisticsDataTable(indexedPlayers, category, seasonRange, byAge);
		addColumns(table, indexedPlayers, category, byAge);
		return table;
	}

	public DataTable getStatisticsDataTable(List<String> players, StatsCategory category, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchStatisticsDataTable(indexedPlayers, category, seasonRange, byAge);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, category, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "IndexedPlayers" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchStatisticsDataTable(IndexedPlayers players, StatsCategory category, Range<Integer> seasonRange, boolean byAge) {
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
				Object value = byAge ? rs.getInt("age") : rs.getInt("season");
				int playerId = rs.getInt("player_id");
				double statsValue = rs.getDouble("value");
				rowCursor.next(value, playerId, statsValue);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, StatsCategory category, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else
			table.addColumn("number", "Season");
		for (String player : players.getPlayers())
			table.addColumn("number", player + " " + category.getName());
	}

	private String getSQL(int playerCount, StatsCategory category, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_STATISTICS_QUERY,
			byAge ? "date_part('year', age((s.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "s.season",
			category.getExpression(),
			byAge ? PLAYER_JOIN : "",
			playerCount == 1 ? "?" : "ANY(?)",
			periodRangeCondition(seasonRange, "s.season"),
			byAge ? "age" : "season"
		);
	}

	private String periodRangeCondition(Range<?> range, String column) {
		String condition = "";
		if (range.hasLowerBound())
			condition += " AND " + column + " >= ?";
		if (range.hasUpperBound())
			condition += " AND " + column + " <= ?";
		return condition;
	}
}
