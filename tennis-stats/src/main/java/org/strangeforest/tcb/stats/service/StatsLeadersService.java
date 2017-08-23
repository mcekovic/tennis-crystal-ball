package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

@Service
public class StatsLeadersService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MinEntries minEntries;

	private static final int MAX_PLAYER_COUNT =  1000;
	private static final int MIN_MATCHES      =   200;
	private static final int MIN_POINTS       = 10000;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE p_%2$s + o_%2$s >= :minEntries%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  WHERE p_%3$s + o_%3$s >= :minEntries%4$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v p USING (player_id)%5$s\n" +
		"ORDER BY %6$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String SUMMED_STATS_LEADERS_COUNT_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT m.player_id, sum(p_%1$s) AS p_%1$s, sum(o_%1$s) AS o_%1$s\n" +
		"  FROM %2$s m%3$s\n" +
		"  INNER JOIN player_v p ON p.player_id = m.player_id%4$s\n" +
		"  GROUP BY m.player_id\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"WHERE p_%1$s + o_%1$s >= :minEntries";

	private static final String SUMMED_STATS_LEADERS_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT m.player_id, %1$s AS value, sum(p_%2$s) AS p_%2$s, sum(o_%2$s) AS o_%2$s\n" +
		"  FROM %3$s m%4$s%5$s\n" +
		"  GROUP BY m.player_id\n" +
		"), stats_leaders AS (\n" +
		"  SELECT player_id, value\n" +
		"  FROM player_stats\n" +
		"  WHERE p_%2$s + o_%2$s >= :minEntries\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v p USING (player_id)%6$s\n" +
		"ORDER BY %7$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String OPPONENT_JOIN = //language=SQL
		"\n  INNER JOIN player_v o ON o.player_id = m.opponent_id";


	@Cacheable("StatsLeaders.Count")
	public int getPlayerCount(String category, PerfStatsFilter filter, Integer minEntries) {
		return Math.min(MAX_PLAYER_COUNT, getPlayerCount(StatsCategory.get(category), filter, minEntries));
	}

	private int getPlayerCount(StatsCategory statsCategory, PerfStatsFilter filter, Integer minEntries) {
		minEntries = getMinEntries(statsCategory, filter, minEntries);
		filter.withPrefix("p.");
		if (filter.isEmptyOrForSeasonOrSurface() && !filter.hasSurfaceGroup()) {
			return jdbcTemplate.queryForObject(
				format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsCategory), filter.getCriteria()),
				filter.getParams().addValue("minEntries", minEntries),
				Integer.class
			);
		}
		else {
			return jdbcTemplate.queryForObject(
				format(SUMMED_STATS_LEADERS_COUNT_QUERY, minEntriesColumn(statsCategory), statsTableName(filter), filter.getOpponentFilter().isOpponentRequired() ? OPPONENT_JOIN : "", where(filter.getCriteria(), 2)),
				filter.getParams().addValue("minEntries", minEntries),
				Integer.class
			);
		}
	}

	@Cacheable("StatsLeaders.Table")
	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String category, int playerCount, PerfStatsFilter filter, Integer minEntries, String orderBy, int pageSize, int currentPage) {
		StatsCategory statsCategory = StatsCategory.get(category);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		filter.withPrefix("p.");
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			getTableSQL(statsCategory, filter, orderBy),
			filter.getParams().addValue("minEntries", getMinEntries(statsCategory, filter, minEntries)).addValue("offset", offset).addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null;
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, active, value, statsCategory.getType()));
			}
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String category, PerfStatsFilter filter, Integer minEntries) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return getMinEntries(statsCategory, filter, minEntries) + (statsCategory.isNeedsStats() ? " points" : " matches");
	}

	private String getTableSQL(StatsCategory statsCategory, PerfStatsFilter filter, String orderBy) {
		return filter.isEmptyOrForSeasonOrSurface() && !filter.hasSurfaceGroup()
	       ? format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy)
	       : format(SUMMED_STATS_LEADERS_QUERY, statsCategory.getSummedExpression(), minEntriesColumn(statsCategory), statsTableName(filter), filter.getOpponentFilter().isOpponentRequired() ? OPPONENT_JOIN : "", where(filter.getBaseCriteria(), 2), where(filter.getSearchCriteria()), orderBy);
	}

	private static String statsTableName(PerfStatsFilter filter) {
		if (filter.isEmptyOrForSeasonOrSurface())
			return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
		else
			return "player_match_stats_v";
	}

	private String minEntriesColumn(StatsCategory category) {
		return category.isNeedsStats() ? "sv_pt" : "matches";
	}

	private int getMinEntries(StatsCategory category, PerfStatsFilter filter, Integer minEntriesOverride) {
		return minEntriesOverride == null ? minEntries.getFilteredMinEntries(category.isNeedsStats() ? MIN_POINTS : MIN_MATCHES, filter) : minEntriesOverride;
	}
}
