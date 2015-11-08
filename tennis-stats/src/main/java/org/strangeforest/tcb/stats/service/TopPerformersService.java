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

	private static final int MAX_PLAYER_COUNT          = 1000;
	private static final int MIN_ENTRIES_SEASON_FACTOR =   10;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_performance\n" +
		"ORDER BY season DESC";

	private static final String TOP_PERFORMERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE %2$s_won + %2$s_lost >= ?%3$s";

	private static final String TOP_PERFORMERS_QUERY = //language=SQL
		"WITH top_performers AS (\n" +
		"  SELECT player_id, name, country_id, %1$s_won::real/(%1$s_won + %1$s_lost) AS won_lost_pct, %1$s_won AS won, %1$s_lost AS lost\n" +
		"  FROM %2$s\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE %1$s_won + %1$s_lost >= ?%3$s\n" +
		"), top_performers_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id, name, country_id, won_lost_pct, won, lost\n" +
		"  FROM top_performers\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, won, lost\n" +
		"FROM top_performers_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %4$s OFFSET ? LIMIT ?";


	public List<Integer> getSeasons() {
		return jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class);
	}

	public int getPlayerCount(String dimension, StatsPlayerListFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, perfTableName(filter), dimension, filter.getCriteria()),
			filter.getParamsWithPrefix(getMinEntriesValue(dimension, filter)),
			Integer.class
		));
	}

	public BootgridTable<TopPerformerRow> getTopPerformersTable(String dimension, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, dimension, perfTableName(filter), filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				WonLost wonLost = mapWonLost(rs);
				table.addRow(new TopPerformerRow(rank, playerId, name, countryId, wonLost));
			},
			filter.getParamsWithPrefix(getMinEntriesValue(dimension, filter), playerCount, offset, pageSize)
		);
		return table;
	}

	public String getTopPerformersMinEntries(String dimension, StatsPlayerListFilter filter) {
		return getMinEntriesValue(dimension, filter) + " " + DIMENSIONS.get(dimension).getEntriesName();
	}

	private static String perfTableName(StatsPlayerListFilter filter) {
		return filter.hasSeason() ? "player_season_performance" : "player_performance";
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntriesValue(String dimension, StatsPlayerListFilter filter) {
		int minEntries = DIMENSIONS.get(dimension).getMinEntries();
		return filter.hasSeason() ? minEntries / MIN_ENTRIES_SEASON_FACTOR : minEntries;
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
