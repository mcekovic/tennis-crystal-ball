package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

@Service
public class StatsLeadersService {

	@Autowired private TournamentService tournamentService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT           =  1000;
	private static final int MIN_MATCHES                =   200;
	private static final int MIN_POINTS                 = 10000;
	private static final int MIN_ENTRIES_SEASON_FACTOR  =    10;
	private static final int MIN_ENTRIES_SURFACE_FACTOR =     4;
	private static final int MIN_ENTRIES_EVENT_FACTOR   =   100;
	private static final Map<Range<Integer>, Integer> MIN_ENTRIES_TOURNAMENT_FACTOR = ImmutableMap.<Range<Integer>, Integer>builder()
		.put(Range.closed(1, 2), 100)
		.put(Range.closed(3, 5), 50)
		.put(Range.closed(6, 9), 25)
		.put(Range.atLeast(5), 20)
	.build();

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
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
		"INNER JOIN player_v USING (player_id)%5$s\n" +
		"ORDER BY %6$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String SUMMED_STATS_LEADERS_COUNT_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, " + StatisticsService.PLAYER_STATS_SUMMED_COLUMNS +
		"  FROM player_match_stats_v\n" +
		"  INNER JOIN player_v USING (player_id)%1$s\n" +
		"  GROUP BY player_id\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"WHERE p_%2$s + o_%2$s >= :minEntries";

	private static final String SUMMED_STATS_LEADERS_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, " + StatisticsService.PLAYER_STATS_SUMMED_COLUMNS +
		"  FROM player_match_stats_v%1$s\n" +
		"  GROUP BY player_id\n" +
		"), stats_leaders AS (\n" +
		"  SELECT player_id, %2$s AS value\n" +
		"  FROM player_stats\n" +
		"  WHERE p_%3$s + o_%3$s >= :minEntries\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v USING (player_id)%4$s\n" +
		"ORDER BY %5$s NULLS LAST OFFSET :offset LIMIT :limit";


	public int getPlayerCount(String category, StatsPlayerListFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, getPlayerCount(StatsCategory.get(category), filter));
	}

	protected int getPlayerCount(StatsCategory statsCategory, StatsPlayerListFilter filter) {
		if (filter.hasTournamentOrTournamentEvent()) {
			return jdbcTemplate.queryForObject(
				format(SUMMED_STATS_LEADERS_COUNT_QUERY, where(filter.getCriteria(), 2), minEntriesColumn(statsCategory)),
				filter.getParams().addValue("minEntries", getMinEntriesValue(statsCategory, filter)),
				Integer.class
			);
		}
		else {
			return jdbcTemplate.queryForObject(
				format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsCategory), filter.getCriteria()),
				filter.getParams().addValue("minEntries", getMinEntriesValue(statsCategory, filter)),
				Integer.class
			);
		}
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String category, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsCategory statsCategory = StatsCategory.get(category);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			getTableSQL(statsCategory, filter, orderBy),
			filter.getParams().addValue("minEntries", getMinEntriesValue(statsCategory, filter)).addValue("offset", offset).addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() && !filter.hasSeason() ? rs.getBoolean("active") : null;
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, active, value, statsCategory.getType()));
			}
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String category, StatsPlayerListFilter filter) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return getMinEntriesValue(statsCategory, filter) + (statsCategory.isNeedsStats() ? " points" : " matches");
	}

	private String getTableSQL(StatsCategory statsCategory, StatsPlayerListFilter filter, String orderBy) {
		return filter.hasTournamentOrTournamentEvent()
	       ? format(SUMMED_STATS_LEADERS_QUERY, where(filter.getBaseCriteria(), 2), statsCategory.getExpression(), minEntriesColumn(statsCategory), where(filter.getSearchCriteria()), orderBy)
	       : format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy);
	}

	private static String statsTableName(StatsPlayerListFilter filter) {
		return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
	}

	private String minEntriesColumn(StatsCategory category) {
		return category.isNeedsStats() ? "sv_pt" : "matches";
	}

	private int getMinEntriesValue(StatsCategory category, StatsPlayerListFilter filter) {
		int minEntries = category.isNeedsStats() ? MIN_POINTS : MIN_MATCHES;
		if (filter.hasSeason()) {
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
			LocalDate today = LocalDate.now();
			if (filter.getSeason() == today.getYear() && today.getMonth().compareTo(Month.SEPTEMBER) <= 0)
				minEntries /= 12.0 / today.getMonth().getValue();
		}
		if (filter.hasSurface())
			minEntries /= MIN_ENTRIES_SURFACE_FACTOR;
		if (filter.hasTournamentEvent())
			minEntries /= MIN_ENTRIES_EVENT_FACTOR;
		else if (filter.hasTournament())
			minEntries /= getMinEntriesTournamentFactor(tournamentService.getTournamentEventCount(filter.getTournamentId()));
		return Math.max(minEntries, 2);
	}

	private int getMinEntriesTournamentFactor(int eventCount) {
		return MIN_ENTRIES_TOURNAMENT_FACTOR.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
