package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

@Service
public class StatsLeadersService {

	@Autowired private JdbcTemplate jdbcTemplate;
	@Autowired private TournamentService tournamentService;

	private static final int MAX_PLAYER_COUNT           =  1000;
	private static final int MIN_MATCHES                =   200;
	private static final int MIN_POINTS                 = 10000;
	private static final int MIN_ENTRIES_SEASON_FACTOR  =    10;
	private static final int MIN_ENTRIES_SURFACE_FACTOR =     2;
	private static final int MIN_ENTRIES_EVENT_FACTOR   =   100;
	private static final Map<Range<Integer>, Integer> MIN_ENTRIES_TOURNAMENT_FACTOR = new HashMap<Range<Integer>, Integer>() {{
		put(Range.closed(1, 2), 100);
		put(Range.closed(3, 5), 50);
		put(Range.closed(6, 9), 25);
		put(Range.atLeast(5), 20);
	}};

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE p_%2$s + o_%2$s >= ?%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  WHERE p_%3$s + o_%3$s >= ?%4$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE rank <= ?%5$s\n" +
		"ORDER BY %6$s NULLS LAST OFFSET ? LIMIT ?";

	private static final String SUMMED_STATS_LEADERS_COUNT_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, " + StatisticsService.PLAYER_STATS_SUMMED_COLUMNS +
		"  FROM player_match_stats_v\n" +
		"  INNER JOIN player_v USING (player_id)\n" +
		"  WHERE TRUE%1$s\n" +
		"  GROUP BY player_id\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"WHERE p_%2$s + o_%2$s >= ?";

	private static final String SUMMED_STATS_LEADERS_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, " + StatisticsService.PLAYER_STATS_SUMMED_COLUMNS +
		"  FROM player_match_stats_v\n" +
		"  WHERE TRUE%1$s\n" +
		"  GROUP BY player_id\n" +
		"), stats_leaders AS (\n" +
		"  SELECT player_id, %2$s AS value\n" +
		"  FROM player_stats\n" +
		"  WHERE p_%3$s + o_%3$s >= ?\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE rank <= ?%4$s\n" +
		"ORDER BY %5$s NULLS LAST OFFSET ? LIMIT ?";


	public int getPlayerCount(String category, StatsPlayerListFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, getPlayerCount(StatsCategory.get(category), filter));
	}

	protected int getPlayerCount(StatsCategory statsCategory, StatsPlayerListFilter filter) {
		if (filter.hasTournamentOrTournamentEvent()) {
			return jdbcTemplate.queryForObject(
				format(SUMMED_STATS_LEADERS_COUNT_QUERY, filter.getBaseCriteria(), minEntriesColumn(statsCategory)),
				filter.getBaseParams(getMinEntriesValue(statsCategory, filter)),
				Integer.class
			);
		}
		else {
			return jdbcTemplate.queryForObject(
				format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria()),
				filter.getBaseParamsWithPrefix(getMinEntriesValue(statsCategory, filter)),
				Integer.class
			);
		}
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String category, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsCategory statsCategory = StatsCategory.get(category);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		String sql = getTableSQL(statsCategory, filter, orderBy);
		Object[] params = getTableParams(statsCategory, playerCount, filter, pageSize, offset);
		jdbcTemplate.query(
			sql,
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				boolean active = !filter.hasSeason() && rs.getBoolean("active");
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, active, value, statsCategory.getType()));
			},
			params
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String category, StatsPlayerListFilter filter) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return getMinEntriesValue(statsCategory, filter) + (statsCategory.isNeedsStats() ? " points" : " matches");
	}

	private String getTableSQL(StatsCategory statsCategory, StatsPlayerListFilter filter, String orderBy) {
		return filter.hasTournamentOrTournamentEvent()
	       ? format(SUMMED_STATS_LEADERS_QUERY, filter.getBaseCriteria(), statsCategory.getExpression(), minEntriesColumn(statsCategory), filter.getSearchCriteria(), orderBy)
	       : format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria(), filter.getSearchCriteria(), orderBy);
	}

	private Object[] getTableParams(StatsCategory statsCategory, int playerCount, StatsPlayerListFilter filter, int pageSize, int offset) {
		return filter.hasTournamentOrTournamentEvent()
			? filter.getParams(asList(getMinEntriesValue(statsCategory, filter), playerCount), offset, pageSize)
			: filter.getParamsWithPrefix(getMinEntriesValue(statsCategory, filter), singletonList(playerCount), offset, pageSize);
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
		if (filter.hasTournamentEvent())
			minEntries /= MIN_ENTRIES_EVENT_FACTOR;
		else if (filter.hasTournament())
			minEntries /= getMinEntriesTournamentFactor(tournamentService.getTournamentEventCount(filter.getTournamentId()));
		return minEntries;
	}

	private int getMinEntriesTournamentFactor(int eventCount) {
		return MIN_ENTRIES_TOURNAMENT_FACTOR.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
