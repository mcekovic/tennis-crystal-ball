package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class StatsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)\n" +
		"LEFT JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"LEFT JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = ? AND set = 0";

	private static final String PLAYER_STATS_QUERY = //language=SQL
		"SELECT sum(minutes) minutes,\n" +
		"  sum(w_ace) w_ace, sum(w_df) w_df, sum(w_sv_pt) w_sv_pt, sum(w_1st_in) w_1st_in, sum(w_1st_won) w_1st_won, sum(w_2nd_won) w_2nd_won, sum(w_sv_gms) w_sv_gms, sum(w_bp_sv) w_bp_sv, sum(w_bp_fc) w_bp_fc,\n" +
		"  sum(l_ace) l_ace, sum(l_df) l_df, sum(l_sv_pt) l_sv_pt, sum(l_1st_in) l_1st_in, sum(l_1st_won) l_1st_won, sum(l_2nd_won) l_2nd_won, sum(l_sv_gms) l_sv_gms, sum(l_bp_sv) l_bp_sv, sum(l_bp_fc) l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)\n" +
		"WHERE m.%1$s = ? AND set = 0%2$s\n";


	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.query(
			MATCH_STATS_QUERY,
			(rs) -> {
				if (rs.next()) {
					String winner = rs.getString("winner");
					String loser = rs.getString("loser");
					PlayerStats winnerStats = mapPlayerStats(rs, "w_");
					PlayerStats loserStats = mapPlayerStats(rs, "l_");
					int minutes = rs.getInt("minutes");
					return new MatchStats(winner, loser, winnerStats, loserStats, minutes);
				}
				else
					return null;
			},
			matchId
		);
	}

	private Object[] playerStatsParams(int playerId, TournamentEventFilter filter) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.addAll(filter.getParamList());
		return params.toArray();
	}

	public PlayerStats getPlayerStats(int playerId, TournamentEventFilter filter) {
		PlayerStats asWinnerStats = jdbcTemplate.queryForObject(
			format(PLAYER_STATS_QUERY, "winner_id", filter.getCriteria()),
			(rs, rowNum) -> {
				return mapPlayerStats(rs, "w_", "l_");
			},
			playerStatsParams(playerId, filter)
		);
		PlayerStats asLoserStats = jdbcTemplate.queryForObject(
			format(PLAYER_STATS_QUERY, "loser_id", filter.getCriteria()),
			(rs, rowNum) -> {
				return mapPlayerStats(rs, "l_", "w_");
			},
			playerStatsParams(playerId, filter)
		);
		return asWinnerStats.add(asLoserStats);
	}

	private PlayerStats mapPlayerStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
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

	private PlayerStats mapPlayerStats(ResultSet rs, String playerPrefix, String opponentPrefix) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, playerPrefix);
		PlayerStats opponentStats = mapPlayerStats(rs, opponentPrefix);
		playerStats.setOpponentStats(opponentStats);
		return playerStats;
	}
}
