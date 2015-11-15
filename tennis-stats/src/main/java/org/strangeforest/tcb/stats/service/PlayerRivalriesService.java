package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static java.util.Arrays.*;

@Service
public class PlayerRivalriesService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_RIVALRIES_QUERY = //language=SQL
		"WITH rivalries_raw AS (\n" +
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
		"), rivalries AS (\n" +
		"  SELECT player_id, opponent_id, sum(matches) matches, sum(won) won, sum(lost) lost\n" +
		"  FROM rivalries_raw\n" +
		"  GROUP BY player_id, opponent_id\n" +
		"  ORDER BY matches DESC, won DESC\n" +
		")\n" +
		"SELECT r.player_id, r.opponent_id, o.name, o.country_id, o.best_rank, r.matches, r.won, r.lost,\n" +
		"  lm.match_id, lm.season, lm.level, lm.surface, lm.tournament, lm.round, lm.winner_id, lm.loser_id, lm.score\n" +
		"FROM rivalries r\n" +
		"LEFT JOIN player_v o ON o.player_id = r.opponent_id,\n" +
		"LATERAL (\n" +
		"  SELECT m.match_id, e.season, e.level, e.surface, e.name AS tournament, m.round, m.winner_id, m.loser_id, m.score\n" +
		"  FROM match m\n" +
		"  LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE (m.winner_id = r.player_id AND m.loser_id = r.opponent_id) OR (m.winner_id = r.opponent_id AND m.loser_id = r.player_id)\n" +
		"  ORDER BY e.date DESC LIMIT 1\n" +
		") lm\n" +
		"WHERE TRUE%1$s\n" +
		"ORDER BY %2$s OFFSET ?";

	public BootgridTable<PlayerRivalryRow> getPlayerRivalriesTable(int player_id, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerRivalryRow> table = new BootgridTable<>(currentPage);
		AtomicInteger rivalries = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_RIVALRIES_QUERY, filter.getCriteria(), orderBy),
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

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"), rs.getInt("matches"));
	}

	private LastMatch mapLastMatch(ResultSet rs) throws SQLException {
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
}
