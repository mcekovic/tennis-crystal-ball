package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class TopPerformersService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String TOP_PERFORMERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_performance\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE %1$s_won + %1$s_lost >= ?%2$s";

	private static final String TOP_PERFORMERS_QUERY = //language=SQL
		"WITH top_performers AS (\n" +
		"  SELECT player_id, name, country_id, %1$s_won::real/(%1$s_won + %1$s_lost) AS won_lost_pct, %1$s_won AS won, %1$s_lost AS lost" +
		"  FROM player_performance\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE %1$s_won + %1$s_lost >= ?%2$s\n" +
		"), top_performers_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id, name, country_id, won_lost_pct, won, lost\n" +
		"  FROM top_performers\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, won, lost\n" +
		"FROM top_performers_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %3$s OFFSET ? LIMIT ?";


	public int getPlayerCount(String dimension, PlayerListFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, dimension, filter.getCriteria()),
			filter.getParamsWithPrefix(getDimensionMinEntries(dimension)),
			Integer.class
		));
	}

	public BootgridTable<TopPerformerRow> getTopPerformersTable(String dimension, int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, dimension, filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				WonLost wonLost = mapWonLost(rs);
				table.addRow(new TopPerformerRow(rank, playerId, name, countryId, wonLost));
			},
			filter.getParamsWithPrefix(getDimensionMinEntries(dimension), playerCount, offset, pageSize)
		);
		return table;
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getDimensionMinEntries(String dimension) {
		return getPerformanceDimension(dimension).getMinEntries();
	}

	public PerformanceDimension getPerformanceDimension(String dimension) {
		return DIMENSIONS.get(dimension);
	}


	// Dimensions

	private static final Map<String, PerformanceDimension> DIMENSIONS = new HashMap<>();
	static {
		// Performance
		addDimension(new PerformanceDimension("matches",                 200, "matches"));
		addDimension(new PerformanceDimension("grand_slam_matches",       50, "Grand Slam matches"));
		addDimension(new PerformanceDimension("masters_matches",          50, "Masters matches"));
		addDimension(new PerformanceDimension("clay_matches",            100, "clay court matches"));
		addDimension(new PerformanceDimension("grass_matches",            50, "grass court matches"));
		addDimension(new PerformanceDimension("hard_matches",            100, "hard court matches"));
		addDimension(new PerformanceDimension("carpet_matches",           50, "carpet court matches"));
		// Pressure situations
		addDimension(new PerformanceDimension("deciding_sets",           100, "matches"));
		addDimension(new PerformanceDimension("fifth_sets",               20, "matches"));
		addDimension(new PerformanceDimension("finals",                   20, "finals"));
		addDimension(new PerformanceDimension("vs_top10",                 20, "matches"));
		addDimension(new PerformanceDimension("after_winning_first_set", 100, "matches"));
		addDimension(new PerformanceDimension("after_losing_first_set",  100, "matches"));
		addDimension(new PerformanceDimension("tie_breaks",              100, "tie breaks"));
	}

	private static void addDimension(PerformanceDimension dimension) {
		DIMENSIONS.put(dimension.getName(), dimension);
	}
}
