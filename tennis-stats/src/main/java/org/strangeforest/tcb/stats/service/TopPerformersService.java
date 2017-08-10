package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.PerformanceService.*;

@Service
public class TopPerformersService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MinEntries minEntries;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_performance\n" +
		"ORDER BY season DESC";

	private static final String TOP_PERFORMERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v USING (player_id)%2$s\n" +
		"%3$s %4$s + %5$s >= :minEntries%6$s";

	private static final String TOP_PERFORMERS_QUERY = //language=SQL
		"WITH top_performers AS (\n" +
		"  SELECT player_id, %1$s::REAL / (%1$s + %2$s) AS won_lost_pct, %1$s AS won, %2$s AS lost, %1$s + %2$s AS played\n" +
		"  FROM %3$s\n" +
		"  WHERE %1$s + %2$s >= :minEntries%4$s\n" +
		"), top_performers_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id, won_lost_pct, won, lost, played\n" +
		"  FROM top_performers\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, won, lost\n" +
		"FROM top_performers_ranked\n" +
		"INNER JOIN player_v USING (player_id)%5$s\n" +
		"ORDER BY %6$s OFFSET :offset LIMIT :limit";


	@Cacheable(value = "Global", key = "'PerformanceSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.getJdbcOperations().queryForList(SEASONS_QUERY, Integer.class);
	}

	@Cacheable("TopPerformers.Count")
	public int getPlayerCount(String category, StatsPerfFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		boolean materializedSum = isMaterializedSum(filter);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, getPerformanceTableName(filter), materializedSum ? "" : where(filter.getCriteria()), materializedSum ? "WHERE" : "GROUP BY player_id HAVING", getPerfColumnName(perfCategory, filter, "_won"), getPerfColumnName(perfCategory, filter, "_lost"), materializedSum ? filter.getCriteria() : ""),
			filter.getParams().addValue("minEntries", getMinEntries(perfCategory, filter)),
			Integer.class
		));
	}

	@Cacheable("TopPerformers.Table")
	public BootgridTable<TopPerformerRow> getTopPerformersTable(String category, int playerCount, StatsPerfFilter filter, String orderBy, int pageSize, int currentPage) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, getPerfColumnName(perfCategory, filter, "_won"), getPerfColumnName(perfCategory, filter, "_lost"), getPerformanceTableName(filter), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy),
			filter.getParams()
				.addValue("minEntries", getMinEntries(perfCategory, filter))
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() && !filter.hasSeason() ? rs.getBoolean("active") : null;
				WonLost wonLost = mapWonLost(rs);
				table.addRow(new TopPerformerRow(rank, playerId, name, countryId, active, wonLost));
			}
		);
		return table;
	}

	private static String getPerfColumnName(PerformanceCategory perfCategory, StatsPerfFilter filter, String suffix) {
		return isMaterializedSum(filter) ? perfCategory.getColumn() + suffix : perfCategory.getSumExpression(suffix);
	}

	public String getTopPerformersMinEntries(String category, StatsPerfFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return getMinEntries(perfCategory, filter) + " " + perfCategory.getEntriesName();
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntries(PerformanceCategory category, StatsPerfFilter filter) {
		return minEntries.getFilteredMinEntries(category.getMinEntries(), filter);
	}
}
