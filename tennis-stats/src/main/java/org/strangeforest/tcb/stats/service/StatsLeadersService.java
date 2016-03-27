package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;

@Service
public class StatsLeadersService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT           =  1000;
	private static final int MIN_MATCHES                =   200;
	private static final int MIN_POINTS                 = 10000;
	private static final int MIN_ENTRIES_SEASON_FACTOR  =    10;
	private static final int MIN_ENTRIES_SURFACE_FACTOR =     2;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE p_%2$s + o_%2$s >= ?%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, name, country_id, active, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  INNER JOIN player_v USING (player_id)\n" +
		"  WHERE p_%3$s + o_%3$s >= ?%4$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, name, country_id, active, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %5$s NULLS LAST OFFSET ? LIMIT ?";


	public int getPlayerCount(String category, StatsPlayerListFilter filter) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsCategory), filter.getCriteria()),
			filter.getParamsWithPrefix(getMinEntriesValue(statsCategory, filter)),
			Integer.class
		));
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String category, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsCategory statsCategory = StatsCategory.get(category);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getCriteria(), orderBy),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				boolean active = !filter.hasSeason() && rs.getBoolean("active");
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, active, value, statsCategory.getType()));
			},
			filter.getParamsWithPrefix(getMinEntriesValue(statsCategory, filter), playerCount, offset, pageSize)
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String category, StatsPlayerListFilter filter) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return getMinEntriesValue(statsCategory, filter) + (statsCategory.isNeedsStats() ? " points" : " matches");
	}

	private static String statsTableName(StatsPlayerListFilter filter) {
		return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
	}

	private String minEntriesColumn(StatsCategory category) {
		return category.isNeedsStats() ? "sv_pt" : "matches";
	}

	private int getMinEntriesValue(StatsCategory category, StatsPlayerListFilter filter) {
		int minEntries = category.isNeedsStats() ? MIN_POINTS : MIN_MATCHES;
		if (filter.hasSeason())
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
		if (filter.hasSurface())
			minEntries /= MIN_ENTRIES_SURFACE_FACTOR;
		return minEntries;
	}
}
