package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
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
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class TopPerformersService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MinEntries minEntries;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_performance\n" +
		"ORDER BY season DESC";


	// Top Performers

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


	// Titles and Results

	private static final String TITLES_AND_RESULTS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)%1$s\n" +
		"WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B')%2$s";

	private static final String TITLES_AND_RESULTS_QUERY = //language=SQL
		"WITH results AS (\n" +
		"  SELECT r.player_id, count(r.result) AS count, max(e.date) AS last_date\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)%1$s\n" +
		"  WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B')%2$s\n" +
		"  GROUP BY r.player_id\n" +
		"), results_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY count DESC) AS rank, player_id, count, last_date\n" +
		"  FROM results\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, count, last_date\n" +
		"FROM results_ranked\n" +
		"INNER JOIN player_v p USING (player_id)%3$s\n" +
		"ORDER BY %4$s OFFSET :offset LIMIT :limit";

	private static final String PLAYER_JOIN = //language=SQL
		"\n  INNER JOIN player_v p USING (player_id)";


	// Mental Toughness

	private static final String MENTAL_POINTS = //language=SQL
		"2 * (coalesce(deciding_sets%1$s, 0) + coalesce(fifth_sets%1$s, 0) + coalesce(finals%1$s, 0)) + coalesce(tie_breaks%1$s, 0) + coalesce(deciding_set_tbs%1$s, 0)";

	private static final String MENTAL_TOUGHNESS_COUNT_QUERY = //language=SQL
		"WITH mental_points AS (\n" +
		"  SELECT player_id, " + getMentalPointsFormula("_won") + " AS points_won, " + getMentalPointsFormula("_lost") + " AS points_lost\n" +
		"  FROM %1$s%2$s\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM mental_points\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE points_won + points_lost >= :minEntries%3$s";

	private static final String MENTAL_TOUGHNESS_QUERY = //language=SQL
		"WITH mental_points AS (\n" +
		"  SELECT player_id, " + getMentalPointsFormula("_won") + " AS points_won, " + getMentalPointsFormula("_lost") + " AS points_lost,\n" +
		"    deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, tie_breaks_won, tie_breaks_lost, deciding_set_tbs_won, deciding_set_tbs_lost\n" +
		"  FROM %1$s%2$s\n" +
		"), mental_toughness AS (\n" +
		"    SELECT player_id, points_won::REAL / nullif(points_lost, 0) rating, points_won, points_lost, deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, tie_breaks_won, tie_breaks_lost, deciding_set_tbs_won, deciding_set_tbs_lost\n" +
		"    FROM mental_points\n" +
		"    WHERE points_won + points_lost >= :minEntries\n" +
		"), mental_toughness_ranked AS (\n" +
		"    SELECT rank() OVER (ORDER BY rating DESC) AS rank, player_id, rating, points_won, points_lost, deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, tie_breaks_won, tie_breaks_lost, deciding_set_tbs_won, deciding_set_tbs_lost\n" +
		"    FROM mental_toughness\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, rating, points_won, points_lost, deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, tie_breaks_won, tie_breaks_lost, deciding_set_tbs_won, deciding_set_tbs_lost\n" +
		"FROM mental_toughness_ranked\n" +
		"INNER JOIN player_v USING (player_id)%3$s\n" +
		"ORDER BY %4$s OFFSET :offset LIMIT :limit";

	private static final String MENTAL_TOUGHNESS_SUMMED = //language=SQL
		"(\n" +
		"  SELECT m.player_id, sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost, sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost, sum(finals_won) finals_won, sum(finals_lost) finals_lost, sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost, sum(deciding_set_tbs_won) deciding_set_tbs_won, sum(deciding_set_tbs_lost) deciding_set_tbs_lost\n" +
		"  FROM player_match_performance_v m%1$s%2$s\n" +
		"  GROUP BY m.player_id\n" +
		") AS mental_toughness_summed";


	@Cacheable(value = "Global", key = "'PerformanceSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.getJdbcOperations().queryForList(SEASONS_QUERY, Integer.class);
	}


	// Top Performers

	@Cacheable("TopPerformers.Count")
	public int getTopPerformersPlayerCount(String category, PerfStatsFilter filter, Integer minEntries) {
		var perfCategory = PerformanceCategory.get(category);
		var materializedSum = isMaterializedSum(filter);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, getTopPerformersTableName(perfCategory, filter), materializedSum ? perfCategory.getColumn() : "items", materializedSum ? filter.getCriteria() : filter.getSearchCriteria()),
			filter.getParams().addValue("minEntries", getMinEntries(perfCategory, filter, minEntries)),
			Integer.class
		));
	}

	@Cacheable("TopPerformers.Table")
	public BootgridTable<TopPerformerRow> getTopPerformersTable(String category, int playerCount, PerfStatsFilter filter, Integer minEntries, String orderBy, int pageSize, int currentPage) {
		var perfCategory = PerformanceCategory.get(category);
		var materializedSum = isMaterializedSum(filter);
		var table = new BootgridTable<TopPerformerRow>(currentPage, playerCount);
		var offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, materializedSum ? perfCategory.getColumn() : "items", getTopPerformersTableName(perfCategory, filter), materializedSum ? filter.getBaseCriteria() : "", where(filter.getSearchCriteria()), orderBy),
			filter.getParams()
				.addValue("minEntries", getMinEntries(perfCategory, filter, minEntries))
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				var rank = rs.getInt("rank");
				var playerId = rs.getInt("player_id");
				var name = rs.getString("name");
				var countryId = getInternedString(rs, "country_id");
				var active = !filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null;
				var wonLost = mapWonLost(rs);
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
		var sb = new StringBuilder();
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		if (filter.hasResult())
			sb.append(EVENT_RESULT_JOIN);
		if (filter.getOpponentFilter().isOpponentRequired())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
	}

	public String getTopPerformersMinEntries(String category, PerfStatsFilter filter, Integer minEntries) {
		var perfCategory = PerformanceCategory.get(category);
		return getMinEntries(perfCategory, filter, minEntries) + " " + perfCategory.getEntriesName();
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntries(PerformanceCategory category, PerfStatsFilter filter, Integer minEntriesOverride) {
		return minEntriesOverride == null ? minEntries.getFilteredMinEntries(category.getMinEntries(), filter) : minEntriesOverride;
	}


	// Titles and Results

	@Cacheable("TitlesAndResults.Count")
	public int getTitlesAndResultsPlayerCount(PerfStatsFilter filter) {
		filter.withPrefix("p.");
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TITLES_AND_RESULTS_COUNT_QUERY, getTitlesAndResultsJoin(filter), filter.getCriteria()),
			filter.getParams(),
			Integer.class
		));
	}

	@Cacheable("TitlesAndResults.Table")
	public BootgridTable<PlayerTitlesRow> getTitlesAndResultsTable(int playerCount, PerfStatsFilter filter, String orderBy, int pageSize, int currentPage) {
		var table = new BootgridTable<PlayerTitlesRow>(currentPage, playerCount);
		var offset = (currentPage - 1) * pageSize;
		filter.withPrefix("p.");
		jdbcTemplate.query(
			format(TITLES_AND_RESULTS_QUERY, getTitlesAndResultsJoin(filter), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy),
			filter.getParams()
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				var rank = rs.getInt("rank");
				var playerId = rs.getInt("player_id");
				var name = rs.getString("name");
				var countryId = getInternedString(rs, "country_id");
				var active = !filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null;
				var count = rs.getInt("count");
				var lastDate = getLocalDate(rs, "last_date");
				table.addRow(new PlayerTitlesRow(rank, playerId, name, countryId, active, count, lastDate));
			}
		);
		return table;
	}

	private static String getTitlesAndResultsJoin(PerfStatsFilter filter) {
		var sb = new StringBuilder();
		if (filter.hasActive() || filter.hasSearchPhrase())
			sb.append(PLAYER_JOIN);
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		return sb.toString();
	}


	// Mental Toughness

	public static final PerformanceCategory MENTAL_TOUGHNESS_CATEGORY = PerformanceCategory.get("matches");

	@Cacheable("MentalToughness.Count")
	public int getMentalToughnessPlayerCount(PerfStatsFilter filter, Integer minEntries) {
		var materializedSum = isMaterializedSum(filter);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
				format(MENTAL_TOUGHNESS_COUNT_QUERY, getMentalToughnessTableName(filter), materializedSum ? where(filter.getBaseCriteria()) : "", filter.getSearchCriteria()),
				filter.getParams().addValue("minEntries", getMinEntries(MENTAL_TOUGHNESS_CATEGORY, filter, minEntries)),
				Integer.class
		));
	}

	@Cacheable("MentalToughness.Table")
	public BootgridTable<MentalToughnessRow> getMentalToughnessTable(int playerCount, PerfStatsFilter filter, Integer minEntries, String orderBy, int pageSize, int currentPage) {
		var materializedSum = isMaterializedSum(filter);
		var table = new BootgridTable<MentalToughnessRow>(currentPage, playerCount);
		var offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
				format(MENTAL_TOUGHNESS_QUERY, getMentalToughnessTableName(filter), materializedSum ? where(filter.getBaseCriteria()) : "", where(filter.getSearchCriteria()), orderBy),
				filter.getParams()
						.addValue("minEntries", getMinEntries(MENTAL_TOUGHNESS_CATEGORY, filter, minEntries))
						.addValue("offset", offset)
						.addValue("limit", pageSize),
				rs -> {
					var rank = rs.getInt("rank");
					var playerId = rs.getInt("player_id");
					var name = rs.getString("name");
					var countryId = getInternedString(rs, "country_id");
					var active = !filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null;
					var points = mapWonLost("points", rs);
					var decidingSets = mapWonLost("deciding_sets", rs);
					var fifthSets = mapWonLost("fifth_sets", rs);
					var finals = mapWonLost("finals", rs);
					var tieBreaks = mapWonLost("tie_breaks", rs);
					var decidingSetTieBreaks = mapWonLost("deciding_set_tbs", rs);
					table.addRow(new MentalToughnessRow(rank, playerId, name, countryId, active, points, decidingSets, fifthSets, finals, tieBreaks, decidingSetTieBreaks));
				}
		);
		return table;
	}

	public String getMentalToughnessMinPoints(PerfStatsFilter filter, Integer minPoints) {
		return getMinEntries(MENTAL_TOUGHNESS_CATEGORY, filter, minPoints) + " Mental Points";
	}

	private static String getMentalPointsFormula(String suffix) {
		return format(MENTAL_POINTS, suffix);
	}

	private static String getMentalToughnessTableName(PerfStatsFilter filter) {
		if (filter.isEmpty())
			return "player_performance";
		else if (filter.isForSeason())
			return "player_season_performance";
		else if (filter.isForTournament())
			return "player_tournament_performance";
		else
			return format(MENTAL_TOUGHNESS_SUMMED, getTopPerformersJoin(filter), where(filter.getBaseCriteria(), 2));
	}

	private static WonLost mapWonLost(String prefix, ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt(prefix + "_won"), rs.getInt(prefix + "_lost"));
	}
}
