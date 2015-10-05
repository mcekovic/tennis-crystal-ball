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
		"SELECT minutes, " +
			"w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc, " +
			"l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc " +
		"FROM match_stats " +
		"WHERE match_id = ? AND set = 0";

	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.queryForObject(MATCH_STATS_QUERY, this::matchStatsMapper, matchId);
	}

	private MatchStats matchStatsMapper(ResultSet rs, int rowNum) throws SQLException {
		int minutes = rs.getInt("minutes");
		Stats winnerStats = mapStats(rs, "w_");
		Stats loserStats = mapStats(rs, "l_");
		return new MatchStats(minutes, winnerStats, loserStats);
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
