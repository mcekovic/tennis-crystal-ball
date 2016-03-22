package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class StatisticsChartService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_SEASON_STATISTICS_QUERY = //language=SQL
		"SELECT %1$s, player_id, %2$s AS value\n" +
		"FROM %3$s s%4$s\n" +
		"WHERE player_id = %5$s%6$s\n" +
		"ORDER BY %7$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getStatisticsDataTable(int playerId, StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerId);
		DataTable table = fetchStatisticsDataTable(indexedPlayers, category, surface, seasonRange, byAge);
		addColumns(table, indexedPlayers, category, surface, byAge);
		return table;
	}

	public DataTable getStatisticsDataTable(List<String> players, StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchStatisticsDataTable(indexedPlayers, category, surface, seasonRange, byAge);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, category, surface, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "Players" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchStatisticsDataTable(IndexedPlayers players, StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		DataTable table = new DataTable();
		RowCursor rowCursor = new IntegerRowCursor(table, players);
		jdbcTemplate.query(
			getSQL(players.getCount(), category, surface, seasonRange, byAge),
			ps -> {
				int index = 1;
				if (players.getCount() == 1)
					ps.setInt(index, players.getPlayerIds().iterator().next());
				else
					bindIntegerArray(ps, index, players.getPlayerIds());
				if (!isNullOrEmpty(surface))
					ps.setString(++index, surface);
				index = bindIntegerRange(ps, index, seasonRange);
			},
			rs -> {
				Object x = byAge ? rs.getInt("age") : rs.getInt("season");
				int playerId = rs.getInt("player_id");
				double y = rs.getDouble("value");
				y = adjustStatsValue(category, y);
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static double adjustStatsValue(StatsCategory category, double statsValue) {
		switch (category.getType()) {
			case PERCENTAGE: return round(statsValue, 10000.0);
			case RATIO: return round(statsValue, 1000.0);
			default: return statsValue;
		}
	}

	private static double round(double value, double by) {
		return Math.round(value * by) / by;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, StatsCategory category, String surface, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else
			table.addColumn("number", "Season");
		for (String player : players.getPlayers()) {
			String label = player + " " + category.getTitle();
			if (!isNullOrEmpty(surface))
				label += " on " + Surface.decode(surface).getText();
			table.addColumn("number", label);
		}
	}

	private String getSQL(int playerCount, StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_STATISTICS_QUERY,
			byAge ? "date_part('year', age((s.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "s.season",
			category.getExpression(),
			isNullOrEmpty(surface) ? "player_season_stats" : "player_season_surface_stats",
			byAge ? PLAYER_JOIN : "",
			playerCount == 1 ? "?" : "ANY(?)",
			conditions(surface, seasonRange, "s.season"),
			byAge ? "age" : "season"
		);
	}

	private String conditions(String surface, Range<?> range, String column) {
		StringBuilder conditions = new StringBuilder();
		if (!isNullOrEmpty(surface))
			conditions.append(" AND s.surface = ?::surface");
		if (range.hasLowerBound())
			conditions.append(" AND ").append(column).append(" >= ?");
		if (range.hasUpperBound())
			conditions.append(" AND ").append(column).append(" <= ?");
		return conditions.toString();
	}
}
