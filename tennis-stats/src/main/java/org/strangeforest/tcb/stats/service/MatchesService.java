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
		"SELECT m.match_id, m.match_num, m.round,\n" +
		"  m.winner_id, pw.short_name AS winner_name, m.winner_seed, m.winner_entry, m.winner_country_id,\n" +
		"  m.loser_id, pl.short_name AS loser_name, m.loser_seed, m.loser_entry, m.loser_country_id,\n" +
		"  array(SELECT ROW(w_games, l_games, w_tb_pt, l_tb_pt) FROM set_score s WHERE s.match_id = m.match_id) AS set_scores, m.outcome\n" +
		"FROM match m\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE m.tournament_event_id = ?\n" +
		"ORDER BY match_num";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_id, e.date, e.tournament_event_id, e.name AS tournament, e.level, e.surface, e.indoor, m.round," +
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
				Object[] setScores = (Object[])rs.getArray("set_scores").getArray();
				List<SetScore> score = new ArrayList<>(5);
				for (Object setScore : setScores)
					score.add(mapSetScore(setScore.toString()));
				TournamentEventMatch match = new TournamentEventMatch(
					rs.getLong("match_id"),
					rs.getString("round"),
					mapMatchPlayerEx(rs, "winner_"),
					mapMatchPlayerEx(rs, "loser_"),
					score,
					rs.getString("outcome")
				);
				draw.addMatch(matchNum, match);
			},
			tournamentEventId
		);
		return draw;
	}

	private static SetScore mapSetScore(String setScore) {
		// (wGames,lGames,wTBPoints,lTBPoints)
		int pos1 = setScore.indexOf(',');
		int wGames = Integer.valueOf(setScore.substring(1, pos1));
		pos1++;
		int pos2 = setScore.indexOf(',', pos1);
		int lGames = Integer.valueOf(setScore.substring(pos1, pos2));
		pos2++;
		int pos3 = setScore.indexOf(',', pos2);
		Integer wTBPoints = pos3 > pos2 ? Integer.valueOf(setScore.substring(pos2, pos3)) : null;
		pos3++;
		int pos4 = setScore.length() - 1;
		Integer lTBPoints = pos4 > pos3 ? Integer.valueOf(setScore.substring(pos3, pos4)) : null;
		return new SetScore(wGames, lGames, wTBPoints, lTBPoints);
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
						rs.getInt("tournament_event_id"),
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

	private static MatchPlayerEx mapMatchPlayerEx(ResultSet rs, String prefix) throws SQLException {
		return new MatchPlayerEx(
			rs.getInt(prefix + "id"),
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			rs.getString(prefix + "entry"),
			rs.getString(prefix + "country_id")
		);
	}
}
