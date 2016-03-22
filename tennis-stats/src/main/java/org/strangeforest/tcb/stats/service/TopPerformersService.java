package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

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
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE %2$s_won + %2$s_lost >= ?%3$s";

	private static final String TOP_PERFORMERS_QUERY = //language=SQL
		"WITH top_performers AS (\n" +
		"  SELECT player_id, name, country_id, %1$s_won::real/(%1$s_won + %1$s_lost) AS won_lost_pct, %1$s_won AS won, %1$s_lost AS lost\n" +
		"  FROM %2$s\n" +
		"  INNER JOIN player_v USING (player_id)\n" +
		"  WHERE %1$s_won + %1$s_lost >= ?%3$s\n" +
		"), top_performers_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id, name, country_id, won_lost_pct, won, lost\n" +
		"  FROM top_performers\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, won, lost\n" +
		"FROM top_performers_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %4$s OFFSET ? LIMIT ?";


	@Cacheable(value = "Global", key = "'PerformanceSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class);
	}

	public int getPlayerCount(String category, StatsPlayerListFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, perfTableName(filter), perfCategory.getColumn(), filter.getCriteria()),
			filter.getParamsWithPrefix(getMinEntriesValue(perfCategory, filter)),
			Integer.class
		));
	}

	public BootgridTable<TopPerformerRow> getTopPerformersTable(String category, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, perfCategory.getColumn(), perfTableName(filter), filter.getCriteria(), orderBy),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				WonLost wonLost = mapWonLost(rs);
				table.addRow(new TopPerformerRow(rank, playerId, name, countryId, wonLost));
			},
			filter.getParamsWithPrefix(getMinEntriesValue(perfCategory, filter), playerCount, offset, pageSize)
		);
		return table;
	}

	public String getTopPerformersMinEntries(String category, StatsPlayerListFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return getMinEntriesValue(perfCategory, filter) + " " + perfCategory.getEntriesName();
	}

	private static String perfTableName(StatsPlayerListFilter filter) {
		return filter.hasSeason() ? "player_season_performance" : "player_performance";
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntriesValue(PerformanceCategory category, StatsPlayerListFilter filter) {
		int minEntries = category.getMinEntries();
		return filter.hasSeason() ? minEntries / MIN_ENTRIES_SEASON_FACTOR : minEntries;
	}
}
