package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.StatsCategory.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

@Service
public class StatisticsChartService {

	@Autowired private PlayerService playerService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_SEASON_STATISTICS_QUERY = //language=SQL
		"SELECT %1$s, player_id, %2$s AS value\n" +
		"FROM %3$s s%4$s\n" +
		"WHERE player_id IN (:playerIds)%5$s%6$s\n" +
		"ORDER BY %7$s, player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";


	public DataTable getStatisticsDataTable(int[] playerIds, StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerIds);
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
		if (players.isEmpty())
			return table;
		RowCursor rowCursor = new IntegerRowCursor(table, players);
		jdbcTemplate.query(
			getSQL(category, surface, seasonRange, byAge),
			getParams(players, surface, seasonRange),
			rs -> {
				Object x = byAge ? rs.getInt("age") : rs.getInt("season");
				int playerId = rs.getInt("player_id");
				Object y = getStatsValue(rs, category);
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static Object getStatsValue(ResultSet rs, StatsCategory category) throws SQLException {
		Type type = category.getType();
		switch (type) {
			case COUNT: return rs.getInt("value");
			case PERCENTAGE: return round(rs.getDouble("value"), 10000.0);
			case RATIO: return round(rs.getDouble("value"), 1000.0);
			default: throw unknownEnum(type);
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

	private String getSQL(StatsCategory category, String surface, Range<Integer> seasonRange, boolean byAge) {
		return format(PLAYER_SEASON_STATISTICS_QUERY,
			byAge ? "date_part('year', age((s.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "s.season",
			category.getExpression(),
			isNullOrEmpty(surface) ? "player_season_stats" : "player_season_surface_stats",
			byAge ? PLAYER_JOIN : "",
			!isNullOrEmpty(surface) ? " AND s.surface = :surface::surface" : "",
			rangeFilter(seasonRange, "s.season", "season"),
			byAge ? "age" : "season"
		);
	}

	private MapSqlParameterSource getParams(IndexedPlayers players, String surface, Range<Integer> seasonRange) {
		MapSqlParameterSource params = params("playerIds", players.getPlayerIds());
		if (!isNullOrEmpty(surface))
			params.addValue("surface", surface);
		addRangeParams(params, seasonRange, "season");
		return params;
	}
}
