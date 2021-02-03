package org.strangeforest.tcb.stats.service;

import java.sql.*;

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
public class TopMatchStatsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;
	private static final int MIN_POINTS = 50;

	private static final String TOP_MATCH_STATS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_match_stats_v m\n" +
		"INNER JOIN player_v p USING (player_id)%1$s\n" +
		"WHERE p_sv_pt + o_sv_pt >= " + MIN_POINTS + " AND %2$s IS NOT NULL AND NOT lower(p.name) LIKE '%%unknown%%'%3$s";

	private static final String TOP_MATCH_STATS_QUERY = //language=SQL
		"WITH top_match_stats AS (\n" +
		"  SELECT match_id, player_id, opponent_id, %1$s AS value\n" +
		"  FROM player_match_stats_v m\n" +
		"  INNER JOIN player_v p USING (player_id)%2$s\n" +
		"  WHERE p_sv_pt + o_sv_pt >= " + MIN_POINTS + " AND %1$s IS NOT NULL AND NOT lower(p.name) LIKE '%%unknown%%'%3$s\n" +
		"), top_match_stats_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, match_id, player_id, opponent_id, value\n" +
		"  FROM top_match_stats\n" +
		")\n" +
		"SELECT tm.rank, tm.player_id, p.name, p.country_id, p.active, tm.opponent_id, o.name AS opponent_name, o.country_id AS opponent_country_id, o.active AS opponent_active,\n" +
		"  m.date, tournament_event_id, e.name AS tournament, e.level, m.best_of, m.surface, m.indoor, es.court_speed, m.round, m.score, m.outcome, tm.player_id = m.winner_id AS winner, tm.value\n" +
		"FROM top_match_stats_ranked tm\n" +
		"INNER JOIN match m USING (match_id)\n" +
		"INNER JOIN player_v p ON p.player_id = tm.player_id\n" +
		"INNER JOIN player_v o ON o.player_id = tm.opponent_id\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN event_stats es USING (tournament_event_id)%4$s\n" +
		"ORDER BY %5$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String EVENT_STATS_JOIN = //language=SQL
		"\nLEFT JOIN event_stats es USING (tournament_event_id)";

	private static final String EVENT_RESULT_JOIN = //language=SQL
		"\n  INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)";

	private static final String OPPONENT_JOIN = //language=SQL
		"\n  INNER JOIN player_v o ON o.player_id = m.opponent_id";


	@Cacheable("TopMatchStats.Count")
	public int getPlayerCount(String category, PerfStatsFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, getPlayerCount(StatsCategory.get(category), filter));
	}

	private int getPlayerCount(StatsCategory statsCategory, PerfStatsFilter filter) {
		filter.withPrefix("p.");
		return jdbcTemplate.queryForObject(
			format(TOP_MATCH_STATS_COUNT_QUERY, getTopMatchStatsJoin(filter), statsCategory.getExpression(), filter.getCriteria()),
			filter.getParams(),
			Integer.class
		);
	}

	@Cacheable("TopMatchStats.Table")
	public BootgridTable<TopMatchStatsRow> getTopMatchStatsTable(String category, int playerCount, PerfStatsFilter filter, String orderBy, int pageSize, int currentPage) {
		var statsCategory = StatsCategory.get(category);
		var table = new BootgridTable<TopMatchStatsRow>(currentPage, playerCount);
		filter.withPrefix("p.");
		var offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOP_MATCH_STATS_QUERY, statsCategory.getExpression(), getTopMatchStatsJoin(filter), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy),
			filter.getParams().addValue("offset", offset).addValue("limit", pageSize),
			rs -> {
				var rank = rs.getInt("rank");
				table.addRow(new TopMatchStatsRow(
					rank,
					rs.getInt("player_id"),
					rs.getString("name"),
					getInternedString(rs, "country_id"),
					!filter.hasActive() && !filter.isTimeLocalized() ? rs.getBoolean("active") : null,
					new PlayerRow(
						rank,
						rs.getInt("opponent_id"),
						rs.getString("opponent_name"),
						getInternedString(rs, "opponent_country_id"),
					   !filter.isTimeLocalized() ? rs.getBoolean("opponent_active") : null
					),
					getLocalDate(rs, "date"),
					rs.getInt("tournament_event_id"),
					rs.getString("tournament"),
					getInternedString(rs, "level"),
					rs.getInt("best_of"),
					getInternedString(rs, "surface"),
					rs.getBoolean("indoor"),
					getInteger(rs, "court_speed"),
					getInternedString(rs, "round"),
					rs.getString("score"),
					getInternedString(rs, "outcome"),
					rs.getBoolean("winner"),
					rs.getDouble("value"),
					statsCategory.getType()
				));
			}
		);
		return table;
	}

	private static String getTopMatchStatsJoin(PerfStatsFilter filter) {
		var sb = new StringBuilder();
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		if (filter.hasResult())
			sb.append(EVENT_RESULT_JOIN);
		if (filter.getOpponentFilter().isOpponentRequired())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
	}

	public int getMinPoints() {
		return MIN_POINTS;
	}
}
