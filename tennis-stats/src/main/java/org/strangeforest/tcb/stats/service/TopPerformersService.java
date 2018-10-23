package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.getInternedString;

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
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE %2$s_won + %2$s_lost >= :minEntries%3$s";

	private static final String TOP_PERFORMERS_QUERY = //language=SQL
		"WITH top_performers AS (\n" +
		"  SELECT player_id, %1$s_won::REAL / (%1$s_won + %1$s_lost) AS won_lost_pct, %1$s_won AS won, %1$s_lost AS lost, %1$s_won + %1$s_lost AS played\n" +
		"  FROM %2$s\n" +
		"  WHERE %1$s_won + %1$s_lost >= :minEntries%3$s\n" +
		"), top_performers_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id, won_lost_pct, won, lost, played\n" +
		"  FROM top_performers\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, won, lost\n" +
		"FROM top_performers_ranked\n" +
		"INNER JOIN player_v USING (player_id)%4$s\n" +
		"ORDER BY %5$s OFFSET :offset LIMIT :limit";

	private static final String TOP_PERFORMERS_SUMMED = //language=SQL
		"(\n" +
		"  SELECT m.player_id, %1$s items_won, %2$s items_lost\n" +
		"  FROM player_match_performance_v m%3$s%4$s\n" +
		"  GROUP BY m.player_id\n" +
		") AS player_performance_summed";

	private static final String EVENT_STATS_JOIN = //language=SQL
		"\nLEFT JOIN event_stats es USING (tournament_event_id)";

	private static final String EVENT_RESULT_JOIN = //language=SQL
		"\n  INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)";

	private static final String OPPONENT_JOIN = //language=SQL
	 	"\n  INNER JOIN player_v o ON o.player_id = m.opponent_id";


	@Cacheable(value = "Global", key = "'PerformanceSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.getJdbcOperations().queryForList(SEASONS_QUERY, Integer.class);
	}

	@Cacheable("TopPerformers.Count")
	public int getPlayerCount(String category, PerfStatsFilter filter, Integer minEntries) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		boolean materializedSum = isMaterializedSum(filter);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, getTopPerformersTableName(perfCategory, filter), materializedSum ? perfCategory.getColumn() : "items", materializedSum ? filter.getCriteria() : filter.getSearchCriteria()),
			filter.getParams().addValue("minEntries", getMinEntries(perfCategory, filter, minEntries)),
			Integer.class
		));
	}

	@Cacheable("TopPerformers.Table")
	public BootgridTable<TopPerformerRow> getTopPerformersTable(String category, int playerCount, PerfStatsFilter filter, Integer minEntries, String orderBy, int pageSize, int currentPage) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		boolean materializedSum = isMaterializedSum(filter);
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, materializedSum ? perfCategory.getColumn() : "items", getTopPerformersTableName(perfCategory, filter), materializedSum ? filter.getBaseCriteria() : "", where(filter.getSearchCriteria()), orderBy),
			filter.getParams()
				.addValue("minEntries", getMinEntries(perfCategory, filter, minEntries))
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = getInternedString(rs, "country_id");
				Boolean active = !filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null;
				WonLost wonLost = mapWonLost(rs);
				table.addRow(new TopPerformerRow(rank, playerId, name, countryId, active, wonLost));
			}
		);
		return table;
	}

	private static boolean isMaterializedSum(PerfStatsFilter filter) {
		return filter.isEmpty() || filter.isForSeason() || filter.isForTournament();
	}

	private static String getTopPerformersTableName(PerformanceCategory perfCategory, PerfStatsFilter filter) {
		if (filter.isEmpty())
			return "player_performance";
		else if (filter.isForSeason())
			return "player_season_performance";
		else if (filter.isForTournament())
			return "player_tournament_performance";
		else
			return format(TOP_PERFORMERS_SUMMED, perfCategory.getSumExpression("_won"), perfCategory.getSumExpression("_lost"), getTopPerformersJoin(filter), where(filter.getBaseCriteria(), 2));
	}

	private static String getTopPerformersJoin(PerfStatsFilter filter) {
		StringBuilder sb = new StringBuilder();
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		if (filter.hasResult())
			sb.append(EVENT_RESULT_JOIN);
		if (filter.getOpponentFilter().isOpponentRequired())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
	}

	public String getTopPerformersMinEntries(String category, PerfStatsFilter filter, Integer minEntries) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return getMinEntries(perfCategory, filter, minEntries) + " " + perfCategory.getEntriesName();
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntries(PerformanceCategory category, PerfStatsFilter filter, Integer minEntriesOverride) {
		return minEntriesOverride == null ? minEntries.getFilteredMinEntries(category.getMinEntries(), filter) : minEntriesOverride;
	}
}
