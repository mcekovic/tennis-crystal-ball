package org.strangeforest.tcb.stats.service;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import com.fasterxml.jackson.databind.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class RivalriesService {

	@Autowired private DataService dataService;
	@Autowired private JdbcTemplate jdbcTemplate;
	private boolean lateralSupported;

	private static final int MIN_GREATEST_RIVALRIES_MATCHES = 10;

	private static final String PLAYER_RIVALRIES_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id player_id, loser_id opponent_id, count(match_id) matches, 0 won, 0 lost\n" +
		"  FROM match_for_rivalry_v\n" +
		"  WHERE winner_id = ?\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id, winner_id, count(match_id), 0, 0\n" +
		"  FROM match_for_rivalry_v\n" +
		"  WHERE loser_id = ?\n" +
		"  GROUP BY loser_id, winner_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id), 0\n" +
		"  FROM match_for_stats_v\n" +
		"  WHERE winner_id = ?\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id, winner_id, 0, 0, count(match_id)\n" +
		"  FROM match_for_stats_v\n" +
		"  WHERE loser_id = ?\n" +
		"  GROUP BY loser_id, winner_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT player_id, opponent_id, sum(matches) matches, sum(won) won, sum(lost) lost\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id, opponent_id\n" +
		"  ORDER BY matches DESC, won DESC\n" +
		")\n" +
		"SELECT r.player_id, r.opponent_id, o.name, o.country_id, o.best_rank, r.matches, r.won, r.lost,\n" +
		"%1$s\n" +
		"FROM rivalries_2 r\n" +
		"LEFT JOIN player_v o ON o.player_id = r.opponent_id%2$s\n" +
		"WHERE TRUE%3$s\n" +
		"ORDER BY %4$s OFFSET ?";

	private static final String RIVALRIES_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id, loser_id, count(match_id) matches, 0 won\n" +
		"  FROM match_for_rivalry_v\n" +
		"  WHERE winner_id = ANY(?) AND loser_id = ANY(?)%1$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id)\n" +
		"  FROM match_for_stats_v\n" +
		"  WHERE winner_id = ANY(?) AND loser_id = ANY(?)%1$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT winner_id player_id_1, loser_id player_id_2, sum(matches) matches, sum(won) won, 0 lost\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id player_id_1, winner_id player_id_2, sum(matches), 0, sum(won)\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"), rivalries_3 AS (\n" +
		"  SELECT rank() OVER r AS rank, player_id_1, player_id_2, sum(matches) matches, sum(won) won, sum(lost) lost\n" +
		"  FROM rivalries_2\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"  WINDOW r AS (\n" +
		"    PARTITION BY CASE WHEN player_id_1 < player_id_2 THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY player_id_1\n" +
		"  )\n" +
		")\n" +
		"SELECT r.player_id_1, p1.name name_1, p1.country_id country_id_1, p1.goat_points goat_points_1,\n" +
		"  r.player_id_2, p2.name name_2, p2.country_id country_id_2, p2.goat_points goat_points_2, r.matches, r.won, r.lost,\n" +
		"%2$s\n" +
		"FROM rivalries_3 r\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = r.player_id_1\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = r.player_id_2%3$s\n" +
		"WHERE r.rank = 1";

	private static final String GREATEST_RIVALRIES_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id, loser_id, count(match_id) matches, 0 won\n" +
		"  FROM match_for_rivalry_v\n" +
		"  WHERE TRUE%1$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id)\n" +
		"  FROM match_for_stats_v\n" +
		"  WHERE TRUE%1$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT winner_id player_id_1, loser_id player_id_2, sum(matches) matches, sum(won) won, 0 lost\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2, matches\n" +
		"  HAVING matches >= ?\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id player_id_1, winner_id player_id_2, sum(matches), 0, sum(won)\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2, matches\n" +
		"  HAVING matches >= ?\n" +
		"), rivalries_3 AS (\n" +
		"  SELECT rank() OVER riv AS rank, player_id_1, player_id_2, sum(matches) matches, sum(won) won, sum(lost) lost, g1.goat_points + g2.goat_points rivalry_goat_points\n" +
		"  FROM rivalries_2\n" +
		"  LEFT JOIN player_goat_points g1 ON g1.player_id = player_id_1\n" +
		"  LEFT JOIN player_goat_points g2 ON g2.player_id = player_id_2\n" +
		"  GROUP BY player_id_1, player_id_2, g1.goat_points, g2.goat_points\n" +
		"  WINDOW riv AS (\n" +
		"    PARTITION BY CASE WHEN g1.goat_points > g2.goat_points OR (g1.goat_points = g2.goat_points AND player_id_1 < player_id_2) THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY g1.goat_points DESC, player_id_1\n" +
		"  )\n" +
		")\n" +
		"SELECT rank() OVER (ORDER BY matches DESC, rivalry_goat_points DESC) AS rivalry_rank, r.player_id_1, p1.name name_1, p1.country_id country_id_1, p1.goat_points goat_points_1,\n" +
		"  r.player_id_2, p2.name name_2, p2.country_id country_id_2, p2.goat_points goat_points_2, r.matches, r.won, r.lost,\n" +
		"%2$s\n" +
		"FROM rivalries_3 r\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = r.player_id_1\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = r.player_id_2%3$s\n" +
		"WHERE rank = 1\n" +
		"ORDER BY %4$s, p1.goat_points + p2.goat_points DESC OFFSET ?";

	private static final String LAST_MATCH_LATERAL = //language=SQL
		"  lm.match_id, lm.season, lm.level, lm.surface, lm.tournament, lm.round, lm.winner_id, lm.loser_id, lm.score";

	private static final String LAST_MATCH_JOIN_LATERAL = //language=SQL
		",\n" +
		"LATERAL (\n" +
		"  SELECT m.match_id, e.season, e.date, e.level, e.surface, e.name tournament, m.round, m.winner_id, m.loser_id, m.score\n" +
		"  FROM match m\n" +
		"  LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE ((m.winner_id = r.%1$s AND m.loser_id = r.%2$s) OR (m.winner_id = r.%2$s AND m.loser_id = r.%1$s))%3$s\n" +
		"  ORDER BY e.date DESC, m.round DESC, m.match_num DESC LIMIT 1\n" +
		") lm";

	private static final String LAST_MATCH_JSON = //language=SQL
		"  (SELECT row_to_json(lm) FROM (\n" +
		"     SELECT m.match_id, e.season, e.date, e.level, e.surface, e.name tournament, m.round, m.winner_id, m.loser_id, m.score\n" +
		"     FROM match m\n" +
		"     LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"     WHERE ((m.winner_id = r.%1$s AND m.loser_id = r.%2$s) OR (m.winner_id = r.%2$s AND m.loser_id = r.%1$s))%3$s\n" +
		"     ORDER BY e.date DESC, m.round DESC, m.match_num DESC LIMIT 1\n" +
		"  ) AS lm) AS last_match";


	@PostConstruct
	private void init() {
		lateralSupported = dataService.getDBServerVersion() >= 90300;
	}

	public BootgridTable<PlayerRivalryRow> getPlayerRivalriesTable(int player_id, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerRivalryRow> table = new BootgridTable<>(currentPage);
		AtomicInteger rivalries = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_RIVALRIES_QUERY,
				lateralSupported ? LAST_MATCH_LATERAL : format(LAST_MATCH_JSON, "player_id", "opponent_id", ""),
				lateralSupported ? format(LAST_MATCH_JOIN_LATERAL, "player_id", "opponent_id", "") : "",
				filter.getCriteria(), orderBy
			),
			(rs) -> {
				if (rivalries.incrementAndGet() <= pageSize) {
					int bestRank = rs.getInt("best_rank");
					int playerId = rs.getInt("opponent_id");
					String name = rs.getString("name");
					String countryId = rs.getString("country_id");
					PlayerRivalryRow row = new PlayerRivalryRow(bestRank, playerId, name, countryId);
					row.setWonLost(mapWonLost(rs));
					row.setLastMatch(mapLastMatch(rs));
					table.addRow(row);
				}
			},
			filter.getParamsWithPrefixes(asList(player_id, player_id, player_id, player_id), offset)
		);
		table.setTotal(offset + rivalries.get());
		return table;
	}

	public RivalryCluster getRivalryCluster(List<Integer> playerIds, RivalryFilter filter) {
		String criteria = filter.getCriteria();
		return new RivalryCluster(jdbcTemplate.query(
			format(RIVALRIES_QUERY,
				criteria,
				lateralSupported ? LAST_MATCH_LATERAL : format(LAST_MATCH_JSON, "player_id_1", "player_id_2", criteria),
				lateralSupported ? format(LAST_MATCH_JOIN_LATERAL, "player_id_1", "player_id_2", criteria) : ""
			),
			ps -> {
				int index = 1;
				bindIntegerArray(ps, index, playerIds);
				bindIntegerArray(ps, ++index, playerIds);
				index = filter.bindParams(ps, index);
				bindIntegerArray(ps, ++index, playerIds);
				bindIntegerArray(ps, ++index, playerIds);
				index = filter.bindParams(ps, index);
				filter.bindParams(ps, index);
			},
			(rs, rowNum) -> {
				RivalryPlayer player1 = mapPlayer(rs, "_1");
				RivalryPlayer player2 = mapPlayer(rs, "_2");
				WonLost wonLost = mapWonLost(rs);
				LastMatch lastMatch = mapLastMatch(rs);
				return new Rivalry(player1, player2, wonLost, lastMatch);
			}
		));
	}

	public BootgridTable<GreatestRivalry> getGreatestRivalriesTable(RivalryFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GreatestRivalry> table = new BootgridTable<>(currentPage);
		AtomicInteger rivalries = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		String criteria = filter.getCriteria();
		jdbcTemplate.query(
			format(GREATEST_RIVALRIES_QUERY,
				criteria,
				lateralSupported ? LAST_MATCH_LATERAL : format(LAST_MATCH_JSON, "player_id_1", "player_id_2", criteria),
				lateralSupported ? format(LAST_MATCH_JOIN_LATERAL, "player_id_1", "player_id_2", criteria) : "",
				orderBy
			),
			ps -> {
				int index = filter.bindParams(ps, 0);
				index = filter.bindParams(ps, index);
				ps.setInt(++index, MIN_GREATEST_RIVALRIES_MATCHES);
				ps.setInt(++index, MIN_GREATEST_RIVALRIES_MATCHES);
				index = filter.bindParams(ps, index);
				ps.setInt(++index, offset);
			},
			(rs) -> {
				if (rivalries.incrementAndGet() <= pageSize) {
					int rank = rs.getInt("rivalry_rank");
					RivalryPlayer player1 = mapPlayer(rs, "_1");
					RivalryPlayer player2 = mapPlayer(rs, "_2");
					WonLost wonLost = mapWonLost(rs);
					LastMatch lastMatch = mapLastMatch(rs);
					table.addRow(new GreatestRivalry(rank, player1, player2, wonLost, lastMatch));
				}
			}
		);
		table.setTotal(offset + rivalries.get());
		return table;
	}

	public int getGreatestRivalriesMinMatches() {
		return MIN_GREATEST_RIVALRIES_MATCHES;
	}


	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"), rs.getInt("matches"));
	}

	private static RivalryPlayer mapPlayer(ResultSet rs, String suffix) throws SQLException {
		return new RivalryPlayer(
			rs.getInt("player_id" + suffix),
			rs.getString("name" + suffix),
			rs.getString("country_id" + suffix),
			rs.getInt("goat_points" + suffix)
		);
	}

	private LastMatch mapLastMatch(ResultSet rs) throws SQLException {
		return lateralSupported ? mapLastMatchLateral(rs) : mapLastMatchJson(rs);
	}

	private LastMatch mapLastMatchLateral(ResultSet rs) throws SQLException {
		return new LastMatch(
			rs.getLong("match_id"),
			rs.getInt("season"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getString("tournament"),
			rs.getString("round"),
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			rs.getString("score")
		);
	}

	private static final ObjectReader READER = new ObjectMapper().reader();

	private LastMatch mapLastMatchJson(ResultSet rs) throws SQLException {
		try {
			JsonNode lastMatch = READER.readTree(rs.getString("last_match"));
			return new LastMatch(
				lastMatch.get("match_id").asLong(),
				lastMatch.get("season").asInt(),
				lastMatch.get("level").asText(),
				lastMatch.get("surface").asText(),
				lastMatch.get("tournament").asText(),
				lastMatch.get("round").asText(),
				lastMatch.get("winner_id").asInt(),
				lastMatch.get("loser_id").asInt(),
				lastMatch.get("score").asText()
			);
		}
		catch (IOException ex) {
			throw new SQLException(ex);
		}
	}
}
