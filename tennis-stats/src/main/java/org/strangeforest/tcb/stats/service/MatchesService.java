package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class MatchesService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_id, e.date, e.level, e.surface, e.name AS tournament, m.round, m.winner_id, pw.name AS winner, m.loser_id, pl.name AS loser, m.score FROM match m\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE (m.winner_id = ? OR m.loser_id = ?)%1$s\n" +
		"ORDER BY %2$s OFFSET ?";


	public BootgridTable<Match> getPlayerMatchesTable(int playerId, MatchFilter filter, String orderBy, int pageSize, int currentPage) {
		int offset = (currentPage - 1) * pageSize;
		AtomicInteger matches = new AtomicInteger();
		BootgridTable<Match> table = new BootgridTable<>(currentPage);
		jdbcTemplate.query(
			format(PLAYER_MATCHES_QUERY, filter.getCriteria(), orderBy),
			(rs) -> {
				if (matches.incrementAndGet() <= pageSize) {
					table.addRow(new Match(
						rs.getLong("match_id"),
						rs.getDate("date"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getString("tournament"),
						rs.getString("round"),
						rs.getInt("winner_id"),
						rs.getString("winner"),
						rs.getInt("loser_id"),
						rs.getString("loser"),
						rs.getString("score")
					));
				}
			},
			params(playerId, filter, offset)
		);
		table.setTotal(offset + matches.get());
		return table;
	}

	private Object[] params(int playerId, MatchFilter filter, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.add(playerId);
		params.addAll(filter.getParamList());
		params.add(offset);
		return params.toArray();
	}
}
