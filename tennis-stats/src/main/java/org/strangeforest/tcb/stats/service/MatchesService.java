package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class MatchesService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_EVENT_MATCHES_QUERY =
		"SELECT m.match_id, m.match_num, m.round, m.score,\n" +
		"  m.winner_id, pw.name AS winner_name, m.winner_seed, m.winner_entry, m.winner_country_id,\n" +
		"  m.loser_id, pl.name AS loser_name, m.loser_seed, m.loser_entry, m.loser_country_id\n" +
		"FROM match m\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE m.tournament_event_id = ?\n" +
		"ORDER BY match_num";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_id, e.date, e.name AS tournament, e.level, e.surface, e.indoor, m.round," +
		"  m.winner_id, pw.name AS winner_name, m.winner_seed, m.winner_entry, m.loser_id, pl.name AS loser_name, m.loser_seed, m.loser_entry, m.score\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE (m.winner_id = ? OR m.loser_id = ?)%1$s\n" +
		"ORDER BY %2$s OFFSET ?";


	public TournamentEventDraw getTournamentEventDraw(int tournamentEventId) {
		TournamentEventDraw draw = new TournamentEventDraw();
		jdbcTemplate.query(
			TOURNAMENT_EVENT_MATCHES_QUERY,
			rs -> {
				short matchNum = rs.getShort("match_num");
				TournamentEventMatch match = new TournamentEventMatch(
					rs.getLong("match_id"),
					rs.getString("round"),
					mapMatchPlayerEx(rs, "winner_"),
					mapMatchPlayerEx(rs, "loser_"),
					rs.getString("score")
				);
				draw.addMatch(matchNum, match);
			},
			tournamentEventId
		);
		return draw;
	}

	public BootgridTable<Match> getPlayerMatchesTable(int playerId, MatchFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<Match> table = new BootgridTable<>(currentPage);
		AtomicInteger matches = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_MATCHES_QUERY, filter.getCriteria(), orderBy),
			rs -> {
				if (matches.incrementAndGet() <= pageSize) {
					table.addRow(new Match(
						rs.getLong("match_id"),
						rs.getDate("date"),
						rs.getString("tournament"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getBoolean("indoor"),
						rs.getString("round"),
						mapMatchPlayer(rs, "winner_"),
						mapMatchPlayer(rs, "loser_"),
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

	static MatchPlayer mapMatchPlayer(ResultSet rs, String prefix) throws SQLException {
		return new MatchPlayer(
			rs.getInt(prefix + "id"),
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			rs.getString(prefix + "entry")
		);
	}

	static MatchPlayerEx mapMatchPlayerEx(ResultSet rs, String prefix) throws SQLException {
		return new MatchPlayerEx(
			rs.getInt(prefix + "id"),
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			rs.getString(prefix + "entry"),
			rs.getString(prefix + "country_id")
		);
	}
}
