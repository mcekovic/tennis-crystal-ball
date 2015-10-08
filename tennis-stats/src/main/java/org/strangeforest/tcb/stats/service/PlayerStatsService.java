package org.strangeforest.tcb.stats.service;

import java.sql.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class PlayerStatsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes, " +
			"w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc, " +
			"l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc " +
		"FROM match_stats " +
		"LEFT JOIN match m USING (match_id) " +
		"LEFT JOIN player_v pw ON m.winner_id = pw.player_id " +
		"LEFT JOIN player_v pl ON m.loser_id = pl.player_id " +
		"WHERE match_id = ? AND set = 0";

	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.query(MATCH_STATS_QUERY, this::matchStatsMapper, matchId);
	}

	private MatchStats matchStatsMapper(ResultSet rs) throws SQLException {
		if (rs.next()) {
			String winner = rs.getString("winner");
			String loser = rs.getString("loser");
			Stats winnerStats = mapStats(rs, "w_");
			Stats loserStats = mapStats(rs, "l_");
			int minutes = rs.getInt("minutes");
			return new MatchStats(winner, loser, winnerStats, loserStats, minutes);
		}
		else
			return null;
	}

	private Stats mapStats(ResultSet rs, String prefix) throws SQLException {
		return new Stats(
			rs.getInt(prefix + "ace"),
			rs.getInt(prefix + "df"),
			rs.getInt(prefix + "sv_pt"),
			rs.getInt(prefix + "1st_in"),
			rs.getInt(prefix + "1st_won"),
			rs.getInt(prefix + "2nd_won"),
			rs.getInt(prefix + "sv_gms"),
			rs.getInt(prefix + "bp_sv"),
			rs.getInt(prefix + "bp_fc")
		);
	}
}
