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

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE %2$s >= :minEntries%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  WHERE %3$s >= :minEntries%4$s\n" +
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
		"  SELECT m.player_id\n" +
		"  FROM %2$s m%3$s\n" +
		"  INNER JOIN player_v p ON p.player_id = m.player_id%4$s\n" +
		"  GROUP BY m.player_id\n" +
		"  HAVING sum(%1$s) >= :minEntries\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM player_stats";

	private static final String SUMMED_STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT m.player_id, %1$s AS value\n" +
		"  FROM %2$s m%3$s%4$s\n" +
		"  GROUP BY m.player_id\n" +
		"  HAVING sum(%5$s) >= :minEntries\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v p USING (player_id)%6$s\n" +
		"ORDER BY %7$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String EVENT_RESULT_JOIN = //language=SQL
		"\n  INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)";

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
				format(SUMMED_STATS_LEADERS_COUNT_QUERY, minEntriesColumn(statsCategory), statsTableName(filter), getStatsLeadersJoin(filter), where(filter.getCriteria(), 2)),
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
		return getMinEntries(statsCategory, filter, minEntries) + " " + (statsCategory.getItem().getText());
	}

	private String getTableSQL(StatsCategory statsCategory, PerfStatsFilter filter, String orderBy) {
		return filter.isEmptyOrForSeasonOrSurface() && !filter.hasSurfaceGroup()
	       ? format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy)
	       : format(SUMMED_STATS_LEADERS_QUERY, statsCategory.getSummedExpression(), statsTableName(filter), getStatsLeadersJoin(filter), where(filter.getBaseCriteria(), 2), minEntriesColumn(statsCategory), where(filter.getSearchCriteria()), orderBy);
	}

	private static String getStatsLeadersJoin(PerfStatsFilter filter) {
		StringBuilder sb = new StringBuilder();
		if (filter.hasResult())
			sb.append(EVENT_RESULT_JOIN);
		if (filter.getOpponentFilter().isOpponentRequired())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
	}

	private static String statsTableName(PerfStatsFilter filter) {
		if (filter.isEmptyOrForSeasonOrSurface())
			return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
		else
			return "player_match_stats_v";
	}

	private String minEntriesColumn(StatsCategory category) {
		return category.getItem().getExpression();
	}

	private int getMinEntries(StatsCategory category, PerfStatsFilter filter, Integer minEntriesOverride) {
		return minEntriesOverride == null ? minEntries.getFilteredMinEntries(category.getItem().getMinEntries(), filter) : minEntriesOverride;
	}
}
