package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

@Service
public class TopPerformersService {

	@Autowired private TournamentService tournamentService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT          = 1000;
	private static final int MIN_ENTRIES_SEASON_FACTOR =   10;
	private static final Map<Range<Integer>, Double> MIN_ENTRIES_TOURNAMENT_FACTOR = ImmutableMap.<Range<Integer>, Double>builder()
		.put(Range.closed(1, 2), 100.0)
		.put(Range.closed(3, 5), 50.0)
		.put(Range.closed(6, 9), 25.0)
		.put(Range.atLeast(5), 20.0)
	.build();

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


	@Cacheable(value = "Global", key = "'PerformanceSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.getJdbcOperations().queryForList(SEASONS_QUERY, Integer.class);
	}

	@Cacheable("TopPerformers.Count")
	public int getPlayerCount(String category, StatsPerfFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(TOP_PERFORMERS_COUNT_QUERY, perfTableName(filter), perfCategory.getColumn(), filter.getCriteria()),
			filter.getParams().addValue("minEntries", getMinEntriesValue(perfCategory, filter)),
			Integer.class
		));
	}

	@Cacheable("TopPerformers.Table")
	public BootgridTable<TopPerformerRow> getTopPerformersTable(String category, int playerCount, StatsPerfFilter filter, String orderBy, int pageSize, int currentPage) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		BootgridTable<TopPerformerRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_PERFORMERS_QUERY, perfCategory.getColumn(), perfTableName(filter), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy),
			filter.getParams()
				.addValue("minEntries", getMinEntriesValue(perfCategory, filter))
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

	public String getTopPerformersMinEntries(String category, StatsPerfFilter filter) {
		PerformanceCategory perfCategory = PerformanceCategory.get(category);
		return getMinEntriesValue(perfCategory, filter) + " " + perfCategory.getEntriesName();
	}

	private static String perfTableName(StatsPerfFilter filter) {
		if (filter.hasSeason())
			return "player_season_performance";
		else if (filter.hasTournament())
			return "player_tournament_performance";
		else
			return "player_performance";
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}

	private int getMinEntriesValue(PerformanceCategory category, StatsPerfFilter filter) {
		int minEntries = category.getMinEntries();
		if (filter.hasSeason()) {
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
			LocalDate today = LocalDate.now();
			if (filter.getSeason() == today.getYear() && today.getMonth().compareTo(Month.SEPTEMBER) <= 0)
				minEntries /= 12.0 / today.getMonth().getValue();
		}
		if (filter.hasTournament())
			minEntries /= getMinEntriesTournamentFactor(tournamentService.getTournamentEventCount(filter.getTournamentId()));
		return Math.max(minEntries, 2);
	}

	private double getMinEntriesTournamentFactor(int eventCount) {
		return MIN_ENTRIES_TOURNAMENT_FACTOR.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
